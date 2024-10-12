package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.TaskInfo;
import com.pcistudio.task.procesor.TaskInfoError;

import java.time.Instant;
import java.util.List;

public interface TaskInfoService {
    //when they get polled they should be marked as processing and executionTime should be in the pass

    List<TaskInfo<Object>> poll(String handlerName, int limit);


    void markTaskCompleted(TaskInfo<Object> task);

    //TODO add a retry count to the task, update errorMessage and status to pending

    void markTaskToRetry(TaskInfo<Object> task, Instant nextRetryTime);

    void markTaskFailed(TaskInfo<Object> task);

    List<TaskInfo<Object>> pollProcessingTimeout(String handlerName);

    void storeError(TaskInfoError taskError);
}
