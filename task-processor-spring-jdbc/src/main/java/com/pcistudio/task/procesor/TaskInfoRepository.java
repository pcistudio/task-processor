package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.mapper.SimpleTaskInfoMapper;
import com.pcistudio.task.procesor.page.Cursor;
import com.pcistudio.task.procesor.page.Pageable;
import com.pcistudio.task.procesor.page.Sort;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskInfoOperations;
import com.pcistudio.task.procesor.util.decoder.MessageDecoding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
class TaskInfoRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;
    private final String partitionId;
    private final SimpleTaskInfoMapper simpleMapper;
    private final TaskInfoCursorPageableFactory taskInfoCursorPageableFactory = new TaskInfoCursorPageableFactory();

    public TaskInfoRepository(JdbcTemplate jdbcTemplate, Clock clock, String partitionId) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
        this.partitionId = partitionId;
        this.simpleMapper = new SimpleTaskInfoMapper(partitionId);
    }

    @Transactional
    public int markToProcess(String tableName, String handlerName, UUID readToken, int limit) {
        Instant now = Instant.now(clock);
        log.debug("Mark To process tableName={} handler={}, execution_time {} for partition_id {} with limit {}",
                tableName, handlerName, now, partitionId, limit);
        int updated = jdbcTemplate.update(
                """
                        update %s
                        SET status=?, version=version + 1, updated_at=?, partition_id=?, read_token=?
                        where execution_time<=? and status = ? and handler_name=?
                        order by execution_time asc
                        limit ?
                        """.formatted(tableName)
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
        log.trace("Read {} records to process from tableName={}, handlerName={}", taskToProcess.size(), tableName, handlerName);
        return taskToProcess;
    }


    public void completeTask(String tableName, TaskInfoOperations taskInfo) {
        Instant now = Instant.now(clock);
        int updated = jdbcTemplate.update(
                """
                        update %s
                        SET status=?, version=version+1, updated_at=?
                        where id=? and status=? and version=? and handler_name=?
                        """.formatted(tableName),
                ProcessStatus.COMPLETED.name(), now, taskInfo.getId(), ProcessStatus.PROCESSING.name(), taskInfo.getVersion(), taskInfo.getHandlerName()
        );


        if (updated == 0) {
            throw new OptimisticLockingFailureException("Task was not updated, task=" + taskInfo.getId() + " in table=" + tableName + " handlerName=" + taskInfo.getHandlerName());
        }
        log.info("Task={} in tableName={},handlerName={} has completed", taskInfo.getId(), tableName, taskInfo.getHandlerName());
        taskInfo.completed();
    }

    //FIXME add retry count. Right now it is wrong
    public void markToRetry(String tableName, TaskInfoOperations task, ProcessStatus oldStatus, ProcessStatus newStatus, Instant nextRetryTime) {
        Instant now = Instant.now(clock);
        int updated = jdbcTemplate.update(
                """
                        update %s
                        SET status=?, version=version+1, updated_at=?, retry_count=?, execution_time=?, partition_id=null
                        where id=? and status = ? and version=?
                        """.formatted(tableName),
                newStatus.name(), now, task.getRetryCount(), nextRetryTime, task.getId(), oldStatus.name(), task.getVersion()
        );

        if (updated == 0) {
            throw new OptimisticLockingFailureException("Task was not updated, task=" + task.getId() + " from status=" + oldStatus + " to status=" + newStatus);
        }
        task.markForRetry();
    }

    public void failTask(String tableName, TaskInfoOperations task) {
        Instant now = Instant.now(clock);
        int updated = jdbcTemplate.update(
                """
                        update %s
                        SET status=?, version=version+1, updated_at=?
                        where id=? and status = ? and version=?
                        """.formatted(tableName),
                ProcessStatus.FAILED.name(), now, task.getId(), ProcessStatus.PROCESSING.name(), task.getVersion()
        );

        if (updated == 0) {
            throw new OptimisticLockingFailureException("Task was not updated, task=" + task.getId() + " from status=" + ProcessStatus.PROCESSING + " to status=" + ProcessStatus.FAILED);
        }
        task.failed();
    }

    public Pageable<TaskInfo> getTasks(String tableName, ProcessStatus processStatus, String pageToken, int limit, Sort sort) {
        Cursor<Instant> instantCursor = taskInfoCursorPageableFactory.decodeCursor(pageToken);
        List<TaskInfo> tasks;
        if (sort == Sort.ASC) {
            tasks=  getOldestTasks(tableName, processStatus, instantCursor, limit);
        } else {
            tasks= getLatestTasks(tableName, processStatus, instantCursor, limit);
        }
        return taskInfoCursorPageableFactory.createPageable(tasks, limit);
    }

    private List<TaskInfo> getLatestTasks(String tableName, ProcessStatus processStatus, Cursor<Instant> pageToken, int limit) {
        if (pageToken == null) {
            return jdbcTemplate.query(
                    "SELECT * FROM %s where status=? order by execution_time desc,id desc limit ?".formatted(tableName),
                    simpleMapper,
                    processStatus.name(),
                    limit
            );
        } else {
            Instant executionTimeOffset = pageToken.offset();
            return jdbcTemplate.query(
                    "SELECT * FROM %s where status=? and (execution_time<? or (execution_time=? and id<?)) order by execution_time desc, id desc limit ?".formatted(tableName),
                    simpleMapper,
                    processStatus.name(),
                    executionTimeOffset,
                    executionTimeOffset,
                    pageToken.id(),
                    limit
            );
        }
    }

    public List<TaskInfo> getOldestTasks(String tableName, ProcessStatus processStatus, Cursor<Instant> pageToken, int limit) {
        if (pageToken == null) {
            return jdbcTemplate.query(
                    "SELECT * FROM %s where status=? order by execution_time asc,id asc limit ?".formatted(tableName),
                    simpleMapper,
                    processStatus.name(),
                    limit
            );
        } else {
            Instant executionTimeOffset = pageToken.offset();
            return jdbcTemplate.query(
                    "SELECT * FROM %s where status=? and (execution_time>? or (execution_time=? and id>?)) order by execution_time asc, id asc limit ?".formatted(tableName),
                    simpleMapper,
                    processStatus.name(),
                    executionTimeOffset,
                    executionTimeOffset,
                    pageToken.id(),
                    limit
            );
        }
    }
}
