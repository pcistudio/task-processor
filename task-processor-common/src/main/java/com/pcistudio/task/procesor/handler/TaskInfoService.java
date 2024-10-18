package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.page.Pageable;
import com.pcistudio.task.procesor.page.Sort;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskInfoError;
import com.pcistudio.task.procesor.task.TaskInfoOperations;

import java.time.Instant;
import java.util.List;

public interface TaskInfoService {
    //when they get polled they should be marked as processing and executionTime should be in the pass

    List<TaskInfo> poll(String handlerName, int limit);

    void markTaskCompleted(TaskInfoOperations task);

    //TODO add a retry count to the task, update errorMessage and status to pending

    void markTaskToRetry(TaskInfoOperations task, Instant nextRetryTime);

    void markTaskFailed(TaskInfoOperations task);

    Pageable<TaskInfo> getTasks(String handlerName, ProcessStatus processStatus, String pageToken, int limit, Sort sort);

    List<TaskInfo> retrieveProcessingTimeoutTasks(String handlerName);

    void requeueTimeoutTask(String handlerName);

    void storeError(TaskInfoError taskError);

    List<TaskInfoError> getTaskErrors(String handlerName, Long taskId);
}
