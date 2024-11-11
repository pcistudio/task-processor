package com.pcistudio.task.procesor.task;


import com.pcistudio.task.procesor.util.Assert;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * TaskInfo class
 * You don't suppose to use this class directly, use TaskInfoReader and TaskInfoWriter instead
 * READ_TOKEN field
 * The table for the task info also has a readToken to make sure that the process that modified is the one that
 * read it back. version is not a good choice in this case because every task can has different versions at the time of read
 */
@Getter
@Builder
public class TaskInfo implements TaskInfoOperations {
    private Long id;
    /**
     * This is for batch writing and for requeue
     */
    private UUID batchId;
    private ProcessStatus status;
    private Instant executionTime;

    //    private Object payload;
    private transient byte[] payloadBytes;

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
