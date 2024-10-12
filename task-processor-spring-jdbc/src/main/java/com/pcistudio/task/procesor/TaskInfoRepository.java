package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.mapper.SimpleTaskInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
class TaskInfoRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleTaskInfoMapper simpleMapper = new SimpleTaskInfoMapper();
    private final Clock clock;

    @Transactional
    public int markToProcess(String tableName, String handlerName, String partitionId, int limit) {
        Instant now = Instant.now(clock);
        log.debug("Mark To process tableName={} handler={}, execution_time {} for partition {} with limit {}",
                tableName, handlerName, now, partitionId, limit);
        int updated = jdbcTemplate.update(
                """
                        update %s
                        SET status=?, version=version + 1, updated_at=?, partition=?
                        where execution_time<? and status = ? and handler_name=? limit ?
                        """.formatted(tableName)
                , ProcessStatus.PROCESSING.name(), now, partitionId, now, ProcessStatus.PENDING.name(), handlerName,  limit
        );
        log.debug("Mark to process {} tasks in tableName={}, handlerName={}", updated, tableName, handlerName);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<TaskInfo<Object>> getTaskToProcess(String tableName, String handlerName) {
        List<TaskInfo<Object>> taskToProcess = jdbcTemplate.query(
                "SELECT * FROM %s WHERE status = ? and execution_time<? and handler_name=? LIMIT 128".formatted(tableName),
                simpleMapper,
                ProcessStatus.PROCESSING.name(),
                Instant.now(clock),
                handlerName

        );
        log.trace("Read {} records to process from tableName={}, handlerName={}", taskToProcess.size(), tableName, handlerName);
        return taskToProcess;
    }


    public void completeTask(String tableName, TaskInfo<Object> taskInfo) {
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
        log.info("Task={} in tableName={},handlerName={} ha completed", taskInfo.getId() , tableName, taskInfo.getHandlerName());
        taskInfo.completed();
    }

    //FIXME add retry count. Right now it is wrong
    public void markToRetry(String tableName, TaskInfo<Object> task, ProcessStatus oldStatus, ProcessStatus newStatus, Instant nextRetryTime) {
        Instant now = Instant.now(clock);
        int updated = jdbcTemplate.update(
                """
                        update %s
                        SET status=?, version=version+1, updated_at=?, retry_count=?, execution_time=?, partition=null
                        where id=? and status = ? and version=?
                        """.formatted(tableName),
                newStatus.name(), now, task.getRetryCount(), nextRetryTime, task.getId(), oldStatus.name(), task.getVersion()
        );

        if (updated == 0) {
            throw new OptimisticLockingFailureException("Task was not updated, task=" + task.getId() + " from status=" + oldStatus + " to status=" + newStatus);
        }
        task.markForRetry();
    }

    public void failTask(String tableName, TaskInfo<Object> task) {
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
}
