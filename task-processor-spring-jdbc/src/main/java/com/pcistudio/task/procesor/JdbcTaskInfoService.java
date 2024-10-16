package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.handler.TaskInfoService;
import com.pcistudio.task.procesor.page.Pageable;
import com.pcistudio.task.procesor.page.Sort;
import com.pcistudio.task.procesor.register.ProcessorRegisterLookup;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskInfoError;
import com.pcistudio.task.procesor.task.TaskInfoOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
public class JdbcTaskInfoService implements TaskInfoService {

    private final StorageResolver storageResolver;
    private final ProcessorRegisterLookup processorRegisterLookup;
    private final TaskInfoRepository taskInfoRepository;
    private final TaskInfoErrorRepository taskInfoErrorRepository;

    public JdbcTaskInfoService(StorageResolver storageResolver, String partitionId, JdbcTemplate jdbcTemplate, Clock clock, ProcessorRegisterLookup processorRegisterLookup) {
        this.storageResolver = storageResolver;
        this.taskInfoRepository = new TaskInfoRepository(jdbcTemplate, clock, partitionId);
        this.taskInfoErrorRepository = new TaskInfoErrorRepository(jdbcTemplate, clock);
        this.processorRegisterLookup = processorRegisterLookup;
    }

    /**
     * This method will poll the tasks from the storage and mark them as PROCESSING
     * The partition will identify the server that is processing the task
     * but in case the 2 servers are started with the same partition
     * we add the readToken concept to guaranty that only the server that changed the status will process the task.
     * If the server is down the task will be timeout and pollProcessingTimeout will be called and send the task back to PENDING
     *
     * @param handlerName
     * @param limit
     * @return
     */
    @Override
    public List<TaskInfo> poll(String handlerName, int limit) {
        String tableName = storageResolver.resolveStorageName(handlerName);
        //Update all the task to be processed
        UUID readToken = UUID.randomUUID();
        int updated = taskInfoRepository.markToProcess(tableName, handlerName, readToken, limit);

        //FIXME this shouldn't be here because if nothing is updated and we have some backlog we will never process it
        if (updated == 0) {
            log.info("No tasks to process in tableName={}, handlerName={}", tableName, handlerName);
            return Collections.emptyList();
        }
        Duration processingExpire = processorRegisterLookup.getProperties(handlerName).getProcessingExpire();
        List<TaskInfo> taskToProcess = taskInfoRepository.getTaskToProcess(tableName, handlerName, readToken, processingExpire);

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
    public void markTaskCompleted(TaskInfoOperations task) {
        String tableName = storageResolver.resolveStorageName(task.getHandlerName());
        if (task.getStatus() != ProcessStatus.PROCESSING) {
            // never should be here
            log.warn("Task is not in PROCESSING status, task={}, handlerName={}, tableName={}. Can not mark COMPLETED. Current status={}", task.getId(), task.getHandlerName(), tableName, task.getStatus());
            return;
        }
        taskInfoRepository.completeTask(tableName, task);
    }

    @Override
    @Transactional
    public void markTaskToRetry(TaskInfoOperations task, Instant nextRetryTime) {
        String tableName = storageResolver.resolveStorageName(task.getHandlerName());
        if (task.getStatus() != ProcessStatus.PROCESSING) {
            // never should be here
            log.warn("Task is not in PROCESSING status, task={}, handlerName={}, tableName={}. Can not mark PENDING. Current status={}", task.getId(), task.getHandlerName(), tableName, task.getStatus());
            return;
        }
        taskInfoRepository.markToRetry(tableName, task, ProcessStatus.PROCESSING, ProcessStatus.PENDING, nextRetryTime);
        task.markForRetry();
        log.info("Task={} mark for retry", task.getId());
    }

    @Override
    @Transactional
    public void markTaskFailed(TaskInfoOperations task) {
        String tableName = storageResolver.resolveStorageName(task.getHandlerName());
        if (task.getStatus() != ProcessStatus.PROCESSING) {
            // never should be here
            log.warn("Task is not in PROCESSING status, task={}, handlerName={}, tableName={}. Can not mark FAILED. Current status={}", task.getId(), task.getHandlerName(), tableName, task.getStatus());
            return;
        }
        taskInfoRepository.failTask(tableName, task);
        task.failed();
        log.info("Task={} failed", task.getId());

    }

    @Override
    public Pageable<TaskInfo> getTasks(String handlerName, ProcessStatus processStatus, String pageToken, int limit, Sort sort) {
        String tableName = storageResolver.resolveStorageName(handlerName);
        return taskInfoRepository.getTasks(tableName, processStatus, pageToken, limit, sort);
    }

    @Override
    public List<TaskInfo> pollProcessingTimeout(String handlerName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeError(TaskInfoError taskError) {
        if (taskError == TaskInfoError.EMPTY) {
            log.warn("Empty error received, nothing to store");
            return;
        }
        String tableName = storageResolver.resolveErrorStorageName(taskError.getHandlerName());
        taskInfoErrorRepository.saveError(tableName, taskError);
        log.info("Stored error processing task: {}", taskError);
    }

    @Override
    public List<TaskInfoError> getTaskErrors(String handlerName, Long taskId) {
        String tableName = storageResolver.resolveErrorStorageName(handlerName);
        List<TaskInfoError> taskErrors = taskInfoErrorRepository.getTaskErrors(tableName, taskId);
        log.info("{} error found for task={}", taskErrors.size(), taskId);
        return taskErrors;
    }
}
