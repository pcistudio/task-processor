package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskInfoError;
import com.pcistudio.task.procesor.task.TaskInfoOperations;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TaskInfoService extends TaskInfoVisibilityService {
    //when they get polled they should be marked as processing and executionTime should be in the pass

    List<TaskInfo> poll(String handlerName, int limit);

    void markTaskCompleted(TaskInfoOperations task);

    //TODO add a retry count to the task, update errorMessage and status to pending

    void markTaskToRetry(TaskInfoOperations task, Instant nextRetryTime);

    void markTaskFailed(TaskInfoOperations task);

    List<TaskInfo> retrieveProcessingTimeoutTasks(String handlerName);

    RequeueResult requeueTimeoutTask(String handlerName);

    void storeError(TaskInfoError taskError);

    List<TaskInfoError> getTaskErrors(String handlerName, Long taskId);

    record RequeueResult(UUID batchId, int updateCount) {
        public static final RequeueResult EMPTY = new RequeueResult(null, 0);

        public boolean isEmpty() {
            return this != EMPTY || updateCount == 0;
        }
    }
}
