package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.handler.TaskInfoService;
import com.pcistudio.task.procesor.mapper.SimpleTaskInfoMapper;
import com.pcistudio.task.procesor.mapper.TaskInfoMapper;
import com.pcistudio.task.procesor.page.Cursor;
import com.pcistudio.task.procesor.page.Pageable;
import com.pcistudio.task.procesor.page.Sort;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskInfoError;
import com.pcistudio.task.procesor.task.TaskInfoOperations;
import com.pcistudio.task.procesor.util.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
class TaskInfoRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;
    private final String partitionId;
    private final SimpleTaskInfoMapper simpleMapper;
    private final TaskInfoCursorPageableFactory taskInfoCursorPageableFactory = new TaskInfoCursorPageableFactory();
    private final ProcessStatusCountRowMapper processStatusCountRowMapper;
    private final TaskInfoMapper taskInfoMapper = new TaskInfoMapper();

    public TaskInfoRepository(JdbcTemplate jdbcTemplate, Clock clock, String partitionId) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
        this.partitionId = partitionId;
        this.simpleMapper = new SimpleTaskInfoMapper(partitionId);
        this.processStatusCountRowMapper = new ProcessStatusCountRowMapper();
    }

    @Transactional
    public int markToProcess(String tableName, String handlerName, UUID readToken, int limit) {
        Instant now = Instant.now(clock);
        log.debug("Mark To process tableName={} handler={}, execution_time {} for partition_id {} with limit {}",
                tableName, handlerName, now, partitionId, limit);
        int updated = jdbcTemplate.update(
                ("update %s %n" +
                        "SET status=?, version=version + 1, updated_at=?, partition_id=?, read_token=? %n" +
                        "where execution_time<=? and status = ? and handler_name=? %n" +
                        "order by execution_time asc %n" +
                        "limit ? %n").formatted(tableName)
                , ProcessStatus.PROCESSING.name(), now, partitionId, readToken.toString(), now, ProcessStatus.PENDING.name(), handlerName, limit
        );
        log.debug("Mark to process {} tasks in tableName={}, handlerName={}", updated, tableName, handlerName);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<TaskInfo> getTaskToProcess(String tableName, String handlerName, UUID readToken, Duration processingExpire) {
        List<TaskInfo> taskToProcess = jdbcTemplate.query(
                "SELECT * FROM %s WHERE status = ? and  updated_at>? and execution_time<=? and handler_name=? and partition_id=? and read_token=? order by execution_time LIMIT 128".formatted(tableName),
                simpleMapper,
                ProcessStatus.PROCESSING.name(),
                Instant.now(clock).minus(processingExpire),
                Instant.now(clock),
                handlerName,
                partitionId,
                readToken.toString()
        );

        if (log.isTraceEnabled()) {
            log.trace("Read {} records to process from tableName={}, handlerName={}", taskToProcess.size(), tableName, handlerName);
        }
        return taskToProcess;
    }

    public void completeTask(String tableName, TaskInfoOperations taskInfo) {
        Instant now = Instant.now(clock);
        int updated = jdbcTemplate.update("update %s SET status=?, version=version+1, updated_at=? where id=? and status=? and version=? and handler_name=?".formatted(tableName),
                ProcessStatus.COMPLETED.name(), now, taskInfo.getId(), ProcessStatus.PROCESSING.name(), taskInfo.getVersion(), taskInfo.getHandlerName()
        );


        if (updated == 0) {
            throw new OptimisticLockingFailureException("Task was not updated, task=" + taskInfo.getId() + " in table=" + tableName + " handlerName=" + taskInfo.getHandlerName());
        }
        if (log.isInfoEnabled()) {
            log.info("Task={} in tableName={},handlerName={} has completed", taskInfo.getId(), tableName, taskInfo.getHandlerName());
        }
        taskInfo.completed();
    }

    public void markToRetry(String tableName, TaskInfoOperations task, ProcessStatus oldStatus, ProcessStatus newStatus, Instant nextRetryTime) {
        Instant now = Instant.now(clock);
        int updated = jdbcTemplate.update(
                "update %s %n SET status=?, version=version+1, updated_at=?, retry_count=?, execution_time=?, partition_id=null %n where id=? and status = ? and version=? and handler_name=? ".formatted(tableName),
                newStatus.name(), now, task.getRetryCount() + 1, nextRetryTime, task.getId(), oldStatus.name(), task.getVersion(), task.getHandlerName()
        );

        if (updated == 0) {
            throw new OptimisticLockingFailureException("Task was not updated, task=%s from status=%s to status=%s".formatted(task.getId(), oldStatus, newStatus));
        }
        task.markForRetry();
    }

    public void failTask(String tableName, TaskInfoOperations task) {
        Instant now = Instant.now(clock);
        int updated = jdbcTemplate.update(
                "update %s SET status=?, version=version+1, updated_at=? where id=? and status = ? and version=? and handler_name=?".formatted(tableName),
                ProcessStatus.FAILED.name(), now, task.getId(), ProcessStatus.PROCESSING.name(), task.getVersion(), task.getHandlerName()
        );

        if (updated == 0) {
            throw new OptimisticLockingFailureException("Task was not updated, task=%s from status=%s to status=%s".formatted(task.getId(), ProcessStatus.PROCESSING, ProcessStatus.FAILED));
        }
        task.failed();
    }

    public void markCorruptTask(String tableName, TaskInfoOperations task) {
        Instant now = Instant.now(clock);
        int updated = jdbcTemplate.update(
                "update %s SET status=?, version=version+1, updated_at=? where id=? and version=? and handler_name=?".formatted(tableName),
                ProcessStatus.CORRUPT_RECORD.name(), now, task.getId(), task.getVersion(), task.getHandlerName()
        );

        if (updated == 0) {
            throw new OptimisticLockingFailureException("Task was not updated, task=" + task.getId() + " from status=" + task.getStatus().name() + " to status=" + ProcessStatus.CORRUPT_RECORD);
        }
    }

    public Pageable<TaskInfo> getTasksRetried(String tableName, String handlerName, String pageToken, int limit) {
        List<TaskInfo> retriedTasks;
        if (pageToken == null) {
            retriedTasks = jdbcTemplate.query(
                    "SELECT * FROM %s WHERE retry_count>0 and handler_name=? limit ?".formatted(tableName),
                    taskInfoMapper,
                    handlerName,
                    limit
            );
        } else {
            Cursor<Instant> curs = taskInfoCursorPageableFactory.decodeCursor(pageToken);
            retriedTasks = jdbcTemplate.query(
                    "SELECT * FROM %s WHERE retry_count>0 and handler_name=? and (execution_time>? or (execution_time=? and id>?)) order by execution_time asc, id asc limit ?".formatted(tableName),
                    taskInfoMapper,
                    handlerName,
                    curs.offset(),
                    curs.offset(),
                    curs.id(),
                    limit
            );
        }

        if (log.isTraceEnabled()) {
            log.trace("Read {} retried records tableName={}, handlerName={}", retriedTasks.size(), tableName, handlerName);
        }
        return taskInfoCursorPageableFactory.createPageable(retriedTasks, limit);
    }

    public Pageable<TaskInfo> getTasks(String tableName, String handlerName, ProcessStatus processStatus, String pageToken, int limit, Sort sort) {
        Cursor<Instant> instantCursor = taskInfoCursorPageableFactory.decodeCursor(pageToken);
        List<TaskInfo> tasks;
        if (sort == Sort.ASC) {
            tasks = getOldestTasks(tableName, handlerName, processStatus, instantCursor, limit);
        } else {
            tasks = getLatestTasks(tableName, handlerName, processStatus, instantCursor, limit);
        }
        return taskInfoCursorPageableFactory.createPageable(tasks, limit);
    }

    private List<TaskInfo> getLatestTasks(String tableName, String handlerName, ProcessStatus processStatus, Cursor<Instant> pageToken, int limit) {
        if (pageToken == null) {
            return jdbcTemplate.query(
                    "SELECT * FROM %s where status=? and handler_name=? order by execution_time desc,id desc limit ?".formatted(tableName),
                    taskInfoMapper,
                    processStatus.name(),
                    handlerName,
                    limit
            );
        } else {
            Instant executionTimeOffset = pageToken.offset();
            return jdbcTemplate.query(
                    "SELECT * FROM %s where status=? and handler_name=? and (execution_time<? or (execution_time=? and id<?)) order by execution_time desc, id desc limit ?".formatted(tableName),
                    taskInfoMapper,
                    processStatus.name(),
                    handlerName,
                    executionTimeOffset,
                    executionTimeOffset,
                    pageToken.id(),
                    limit
            );
        }
    }

    public List<TaskInfo> getOldestTasks(String tableName, String handlerName, ProcessStatus processStatus, Cursor<Instant> pageToken, int limit) {
        if (pageToken == null) {
            return jdbcTemplate.query(
                    "SELECT * FROM %s where status=? and handler_name=? order by execution_time asc,id asc limit ?".formatted(tableName),
                    taskInfoMapper,
                    processStatus.name(),
                    handlerName,
                    limit
            );
        } else {
            Instant executionTimeOffset = pageToken.offset();
            return jdbcTemplate.query(
                    "SELECT * FROM %s where status=? and handler_name=? and (execution_time>? or (execution_time=? and id>?)) order by execution_time asc, id asc limit ?".formatted(tableName),
                    taskInfoMapper,
                    processStatus.name(),
                    handlerName,
                    executionTimeOffset,
                    executionTimeOffset,
                    pageToken.id(),
                    limit
            );
        }
    }

    public TaskInfoService.RequeueResult requeueProcessingTimeoutTask(String tableName, String handlerName, Duration processingExpire) {
        Instant now = Instant.now(clock);
        UUID batchId = UUID.randomUUID();
        int updated = jdbcTemplate.update(
                "update %s SET status=?, version=version+1, updated_at=?, batch_id=?, partition_id=null where status=? and updated_at<? and handler_name=?".formatted(tableName),
                ProcessStatus.PENDING.name(), now, batchId.toString(), ProcessStatus.PROCESSING.name(), now.minus(processingExpire), handlerName
        );

        if (updated == 0) {
            log.info("No timeout task found for handlerName={}, tableName={}", handlerName, tableName);
            return TaskInfoService.RequeueResult.EMPTY;
        } else {
            log.info("Requeue {} tasks in tableName={}, handlerName={}, batchId={}", updated, tableName, handlerName, batchId);
        }
        return new TaskInfoService.RequeueResult(batchId, updated);
    }

    // TODO Expose this in an endpoint

    /**
     * This method will give you the tasks in PROCESSING state that will be ready for requeue at the specified date
     *
     * @param tableName
     * @param handlerName
     * @param processingExpire
     * @param date
     * @return
     */
    @Nullable
    public List<TaskInfo> retrieveRequeueForecast(String tableName, String handlerName, Duration processingExpire, Instant date) {
        return jdbcTemplate.query(
                "select * from %s where status=? and updated_at<? and handler_name=? limit 100 sort by execution_time asc".formatted(tableName),
                taskInfoMapper,
                ProcessStatus.PROCESSING.name(), date.minus(processingExpire), handlerName
        );
    }

    public List<TaskInfoError> createBatchTaskInfoError(String tableName, String handlerName, UUID batchId, String errorMessage) {
        Assert.notNull(errorMessage, "errorMessage must not be null");
        RowMapper<TaskInfoError> rowMapper = (rs, rowNum) -> TaskInfoError.builder()
                .handlerName(handlerName)
                .taskId(rs.getLong("id"))
                .partitionId(rs.getString("partition_id"))
                .errorMessage(errorMessage)
                .createdAt(Instant.now(clock))
                .build();
        return jdbcTemplate.query(
                "SELECT id,partition_id FROM %s where batch_id=?".formatted(tableName),
                rowMapper,
                batchId.toString()
        );
    }

    public List<TaskInfo> retrieveProcessingTimeoutTasks(String tableName, String handlerName, Duration processingExpire) {
        return jdbcTemplate.query(
                "SELECT * FROM %s where status=? and updated_at<? and handler_name=?".formatted(tableName),
                simpleMapper,
                ProcessStatus.PROCESSING.name(),
                Instant.now(clock).minus(processingExpire),
                handlerName
        );
    }

    public Map<String, Integer> getStats(String tableName, LocalDate date) {
        Instant startTime = date.atStartOfDay(clock.getZone()).toInstant();
        Instant endTime = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();

        List<ProcessStatusCount> processStatusCounts = jdbcTemplate.query(
                ("select status, count(*) as ct %n" +
                        "from %s %n" +
                        "where execution_time between ? and ? %n" +
                        "group by status %n" +
                        "union all %n" +
                        "select 'RETRY' as status, sum(retry_count) as ct %n" +
                        "from %s %n" +
                        "where execution_time between ? and ? %n").formatted(tableName, tableName),
                processStatusCountRowMapper,
                startTime,
                endTime,
                startTime,
                endTime
        );

        return processStatusCounts.stream()
                .collect(Collectors.toMap(
                                ProcessStatusCount::status,
                                ProcessStatusCount::count
                        )
                );
    }

    public Integer getCount(String tableName, LocalDate date) {
        Instant startTime = date.atStartOfDay(clock.getZone()).toInstant();
        Instant endTime = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();

        return jdbcTemplate.queryForObject("select count(*) as ct from %s where execution_time between ? and ?".formatted(tableName),
                Integer.class,
                startTime,
                endTime
        );
    }

    record ProcessStatusCount(String status, int count) {
    }

    private static final class ProcessStatusCountRowMapper implements RowMapper<ProcessStatusCount> {

        @Override
        public ProcessStatusCount mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ProcessStatusCount(rs.getString("status"), rs.getInt("ct"));
        }
    }
}
