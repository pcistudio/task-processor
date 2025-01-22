package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.handler.TaskInfoService;
import com.pcistudio.task.procesor.page.Pageable;
import com.pcistudio.task.procesor.page.Sort;
import com.pcistudio.task.procesor.register.HandlerLookup;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskInfoError;
import com.pcistudio.task.procesor.task.TaskInfoOperations;
import com.pcistudio.task.procesor.util.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Slf4j
public class JdbcTaskInfoService implements TaskInfoService {

    private final StorageResolver storageResolver;
    private final HandlerLookup handlerLookup;
    private final TaskInfoRepository taskInfoRepository;
    private final TaskInfoErrorRepository taskInfoErrorRepository;

    public JdbcTaskInfoService(StorageResolver storageResolver, String partitionId, JdbcTemplate jdbcTemplate, Clock clock, HandlerLookup handlerLookup) {
        this.storageResolver = storageResolver;
        this.taskInfoRepository = new TaskInfoRepository(jdbcTemplate, clock, partitionId);
        this.taskInfoErrorRepository = new TaskInfoErrorRepository(jdbcTemplate, clock);
        this.handlerLookup = handlerLookup;
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

        if (updated == 0) {
            log.info("No tasks to process in tableName={}, handlerName={}", tableName, handlerName);
            return Collections.emptyList();
        }
        Duration processingExpire = handlerLookup.getProperties(handlerName).getProcessingExpire();
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
        return removeCorruptRecords(tableName, taskToProcess);
    }

    private List<TaskInfo> removeCorruptRecords(String tableName, List<TaskInfo> taskToProcess) {
        Iterator<TaskInfo> iterator = taskToProcess.iterator();
        while (iterator.hasNext()) {
            TaskInfo taskInfo = iterator.next();
            if (taskInfo.getStatus() == ProcessStatus.CORRUPT_RECORD) {
                iterator.remove();
                taskInfoRepository.markCorruptTask(tableName, taskInfo);
            }
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

        if (log.isInfoEnabled()) {
            log.info("Task={} mark for retry", task.getId());
        }
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

        if (log.isInfoEnabled()) {
            log.info("Task={} failed", task.getId());
        }

    }

    @Override
    public Pageable<TaskInfo> getTasks(String handlerName, ProcessStatus processStatus, String pageToken, int limit, Sort sort) {
        String tableName = storageResolver.resolveStorageName(handlerName);
        return taskInfoRepository.getTasks(tableName, handlerName, processStatus, pageToken, limit, sort);
    }

    @Override
    public Pageable<TaskInfo> getTasksRetried(String handlerName, String pageToken, int limit) {
        String tableName = storageResolver.resolveStorageName(handlerName);
        return taskInfoRepository.getTasksRetried(tableName, handlerName, pageToken, limit);
    }


    @Override
    public List<TaskInfo> retrieveProcessingTimeoutTasks(String handlerName) {
        Duration processingExpire = handlerLookup.getProperties(handlerName).getProcessingExpire();
        Duration processingGracePeriod = handlerLookup.getProperties(handlerName).getProcessingGracePeriod();

        String tableName = storageResolver.resolveStorageName(handlerName);
        return taskInfoRepository.retrieveProcessingTimeoutTasks(tableName, handlerName, processingExpire.plus(processingGracePeriod));
    }

    @Override
    public RequeueResult requeueTimeoutTask(String handlerName) {
        Duration processingExpire = handlerLookup.getProperties(handlerName).getProcessingExpire();
        Duration processingGracePeriod = handlerLookup.getProperties(handlerName).getProcessingGracePeriod();
        String tableName = storageResolver.resolveStorageName(handlerName);
        RequeueResult requeueResult = taskInfoRepository.requeueProcessingTimeoutTask(tableName, handlerName, processingExpire.plus(processingGracePeriod));

        if (!requeueResult.isEmpty()) {
            UUID batchId = requeueResult.batchId();
            Assert.notNull(batchId, "BatchId is null");

            List<TaskInfoError> timeoutTaskInfoError = taskInfoRepository.createBatchTaskInfoError(tableName, handlerName, batchId, "Processing timeout");

            if (log.isInfoEnabled()) {
                log.info("Timeout task found for handlerName={}, tableName={}, batchId={}, errors={}", handlerName, tableName, batchId, timeoutTaskInfoError.size());
            }
            String errorTableName = storageResolver.resolveErrorStorageName(handlerName);
            taskInfoErrorRepository.saveErrors(errorTableName, timeoutTaskInfoError);
        }

        return requeueResult;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeError(TaskInfoError taskError) {
        if (taskError == TaskInfoError.EMPTY) {
            log.warn("Empty error received, nothing to store");
            return;
        }
        String handlerName = taskError.getHandlerName();
        Assert.notNull(handlerName, "Error without handlerName, nothing to store");

        String tableName = storageResolver.resolveErrorStorageName(handlerName);
        taskInfoErrorRepository.saveError(tableName, taskError);

        if (log.isInfoEnabled()) {
            log.info("Stored error processing task: {}", taskError);
        }
    }

    @Override
    public List<TaskInfoError> getTaskErrors(String handlerName, Long taskId) {
        String tableName = storageResolver.resolveErrorStorageName(handlerName);
        List<TaskInfoError> taskErrors = taskInfoErrorRepository.getTaskErrors(tableName, taskId);

        if (log.isInfoEnabled()) {
            log.info("{} error found for task={}", taskErrors.size(), taskId);
        }
        return taskErrors;
    }

    @Override
    public Map<String, Integer> stats(String handlerName, LocalDate date) {
        String tableName = storageResolver.resolveStorageName(handlerName);
        return taskInfoRepository.getStats(tableName, date);
    }

    @Override
    public int count(String handlerName, LocalDate date) {
        String tableName = storageResolver.resolveStorageName(handlerName);
        return taskInfoRepository.getCount(tableName, date);
    }

}
