package com.pcistudio.task.procesor.task;

import com.pcistudio.task.procesor.util.ExceptionUtils;

import java.util.Objects;

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

        return TaskInfoError.builder()
                .taskId(Objects.requireNonNull(getId(), "Id must not be null"))
                .partitionId(Objects.requireNonNull(getPartitionId(), "PartitionId must not be null"))
                .errorMessage(errorMessage)
                .handlerName(Objects.requireNonNull(getHandlerName(), "HandlerName must not be null"))
                .build();
    }

    default TaskInfoError createError(Exception exception) {
        return createError(ExceptionUtils.getMostSpecificCause(exception).getMessage());
    }

}
