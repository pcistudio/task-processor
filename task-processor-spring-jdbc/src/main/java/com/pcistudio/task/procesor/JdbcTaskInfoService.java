package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.handler.TaskInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JdbcTaskInfoService implements TaskInfoService {

    private final StorageResolver storageResolver;
    private final String partitionId;
    private final TaskInfoRepository taskInfoRepository;
    private final TaskInfoErrorRepository taskInfoErrorRepository;


    public JdbcTaskInfoService(StorageResolver storageResolver, String partitionId, JdbcTemplate jdbcTemplate, Clock clock) {
        this.storageResolver = storageResolver;
        this.partitionId = partitionId;
        this.taskInfoRepository = new TaskInfoRepository(jdbcTemplate, clock);
        this.taskInfoErrorRepository = new TaskInfoErrorRepository(jdbcTemplate, clock);
    }

    @Override
    public List<TaskInfo<Object>> poll(String handlerName, int limit) {
        String tableName = storageResolver.resolveStorageName(handlerName);
        //Update all the task to be processed
        int updated = taskInfoRepository.markToProcess(tableName, handlerName, partitionId, limit);

        if (updated == 0) {
            log.info("No tasks to process in tableName={}, handlerName={}", tableName, handlerName);
            return Collections.emptyList();
        }

        List<TaskInfo<Object>> taskToProcess = taskInfoRepository.getTaskToProcess(tableName, handlerName);

        if (updated != taskToProcess.size()) {
            //TODO explain what happen when is greater and when is less
            log.warn("Number of tasks={} to process is different from the number of tasks updated={} in tableName={}, handlerName={}",
                    taskToProcess.size(), updated, tableName, handlerName);
        }
        if (taskToProcess.size() > limit) {
            log.warn("More tasks were returned than the limit={} in tableName={}, handlerName={}." +
                            " System is failing to move task back to PENDING status",
                    limit, tableName, handlerName);
        }
        return taskToProcess;
    }

    @Override
    public void markTaskCompleted(TaskInfo<Object> task) {
        String tableName = storageResolver.resolveStorageName(task.getHandlerName());
        if (task.getStatus() != ProcessStatus.PROCESSING) {
            // never should be here
            log.warn("Task is not in PROCESSING status, task={}, handlerName={}", task.getId(), task.getHandlerName());
            return;
        }
        taskInfoRepository.completeTask(tableName, task);
    }

    @Override
    public void markTaskToRetry(TaskInfo<Object> task, Instant nextRetryTime) {
        String tableName = storageResolver.resolveStorageName(task.getHandlerName());
        if (task.getStatus() != ProcessStatus.PROCESSING) {
            // never should be here
            log.warn("Task is not in PROCESSING status, task={}, handlerName={}", task.getId(), task.getHandlerName());
            return;
        }
        taskInfoRepository.markToRetry(tableName, task, ProcessStatus.PROCESSING, ProcessStatus.PENDING, nextRetryTime);
        task.markForRetry();
        log.info("Task={} mark for retry", task.getId());
    }

    @Override
    public void markTaskFailed(TaskInfo<Object> task) {
        String tableName = storageResolver.resolveStorageName(task.getHandlerName());
        if (task.getStatus() != ProcessStatus.PROCESSING) {
            // never should be here
            log.warn("Task is not in PROCESSING status, task={}, handlerName={}", task.getId(), task.getHandlerName());
            return;
        }
        taskInfoRepository.failTask(tableName, task);
        task.failed();
        log.info("Task={} failed", task.getId());
    }

    @Override
    public List<TaskInfo<Object>> pollProcessingTimeout(String handlerName) {
        return List.of();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeError(TaskInfoError taskError) {
        String tableName = storageResolver.resolveErrorStorageName(taskError.getHandlerName());
        taskInfoErrorRepository.saveError(tableName, taskError);
        log.info("Stored error processing task: {}", taskError);
    }
}
