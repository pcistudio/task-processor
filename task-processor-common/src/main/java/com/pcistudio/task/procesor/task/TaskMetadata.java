package com.pcistudio.task.procesor.task;

import com.pcistudio.task.procesor.util.Assert;
import com.pcistudio.task.procesor.util.ExceptionUtils;

public interface TaskMetadata {
    Long getId();

    java.util.UUID getBatchId();

    ProcessStatus getStatus();

    Long getVersion();

    java.time.Instant getCreatedAt();

    java.time.Instant getUpdatedAt();

    java.time.Instant getExecutionTime();

    int getRetryCount();

    String getHandlerName();

    String getPartitionId();

    String getObjectType();

    default TaskInfoError createError(String errorMessage) {
        if (errorMessage == null) {
            return TaskInfoError.EMPTY;
        }
        Assert.notNull(getId(), "Id must not be null");
        Assert.notNull(getPartitionId(), "PartitionId must not be null");
        Assert.notNull(getHandlerName(), "HandlerName must not be null");

        return TaskInfoError.builder()
                .taskId(getId())
                .partitionId(getPartitionId())
                .errorMessage(errorMessage)
                .handlerName(getHandlerName())
                .build();
    }

    default TaskInfoError createError(Exception exception) {
        return createError(ExceptionUtils.getMostSpecificCause(exception).getMessage());
    }

}
