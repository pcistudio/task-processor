package com.pcistudio.task.procesor.task;


import com.pcistudio.task.procesor.util.Assert;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * TaskInfo class
 * You don't suppose to use this class directly, use TaskInfoReader and TaskInfoWriter instead
 */
@Getter
@Builder
public class TaskInfo implements TaskInfoOperations {
    private Long id;
    private UUID batchId;
    private ProcessStatus status;
    private Instant executionTime;

    //    private Object payload;
    private byte[] payloadBytes;

    private String handlerName;
    private String partitionId;
    private String objectType;

    // no exposed to the user
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
    private int retryCount;

    public void incrementRetryCount() {
        retryCount++;
    }

    public void updateStatus(ProcessStatus status) {
        this.status = status;
    }

    public void updateUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void incrementVersion() {
        version++;
    }

    public void setId(Long id) {
        Assert.notNull(id, "Id must not be null");
        if (this.id != null) {
            throw new IllegalStateException("Id can only be set once");
        }
        this.id = id;
    }

    @Override
    public String toString() {
        return "TaskInfo{" +
                "id=" + id +
                ", batchId=" + batchId +
                ", status=" + status +
//                ", lastErrorMessage='" + lastErrorMessage + '\'' +
                ", version=" + version +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
//                ", payload=" + payload +
                ", retryCount=" + retryCount +
                ", handlerName='" + handlerName + '\'' +
                ", objectType='" + objectType + '\'' +
                '}';
    }

    public void completed() {
        version = version + 1;
        updateStatus(ProcessStatus.COMPLETED);
    }

    public void markForRetry() {
        version = version + 1;
        updateStatus(ProcessStatus.PENDING);
    }

    public void failed() {
        version = version + 1;
        updateStatus(ProcessStatus.FAILED);
    }
}
