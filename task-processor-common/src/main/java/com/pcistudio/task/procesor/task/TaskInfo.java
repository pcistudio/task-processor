package com.pcistudio.task.procesor.task;


import com.pcistudio.task.procesor.util.Assert;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * TaskInfo class
 * You don't suppose to use this class directly, use TaskInfoReader and TaskInfoWriter instead
 * READ_TOKEN field
 * The table for the task info also has a readToken to make sure that the process that modified is the one that
 * read it back. version is not a good choice in this case because every task can has different versions at the time of read
 */
@Getter
//@Builder
public final class TaskInfo implements TaskInfoOperations {
    @Nullable
    private Long id;
    /**
     * This is for batch writing and for requeue
     */
    @Nullable
    private UUID batchId;
    private ProcessStatus status;
    private Instant executionTime;

    @SuppressFBWarnings({"EI_EXPOSE_REP"})
    private transient byte[] payloadBytes;

    private String handlerName;
    @Nullable
    private String partitionId;

    /**
     * The type of the object that is stored in the payload
     * It is only for information purposes because if the payload is decode in other app it will not have the type
     * or could not be in the same package
     */
    @Nullable
    private String objectType;

    // no exposed to the user
    private Long version;
    @Nullable
    private Instant createdAt;
    @Nullable
    private Instant updatedAt;
    private int retryCount;

    private TaskInfo(TaskInfoBuilder taskInfoBuilder) {
        this.id = taskInfoBuilder.id;
        this.batchId = taskInfoBuilder.batchId;
        this.status = Objects.requireNonNull(taskInfoBuilder.status, "status must not be null");
        this.executionTime = Objects.requireNonNull(taskInfoBuilder.executionTime, "executionTime must not be null");
        this.payloadBytes = Objects.requireNonNull(taskInfoBuilder.payloadBytes, "payloadBytes must not be null");
        this.handlerName = Objects.requireNonNull(taskInfoBuilder.handlerName, "handlerName must not be null");
        this.partitionId = taskInfoBuilder.partitionId;
        this.objectType = taskInfoBuilder.objectType;
        this.version = Objects.requireNonNull(taskInfoBuilder.version, "version must not be null");
        this.createdAt = taskInfoBuilder.createdAt;
        this.updatedAt = taskInfoBuilder.updatedAt;
        this.retryCount = taskInfoBuilder.retryCount;
    }

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

    public void setId(@Nullable Long id) {
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
                ", version=" + version +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", retryCount=" + retryCount +
                ", handlerName='" + handlerName + '\'' +
                ", objectType='" + objectType + '\'' +
                '}';
    }

    @Override
    public void completed() {
        version = version + 1;
        updateStatus(ProcessStatus.COMPLETED);
    }

    @Override
    public void markForRetry() {
        version = version + 1;
        updateStatus(ProcessStatus.PENDING);
    }

    @Override
    public void failed() {
        version = version + 1;
        updateStatus(ProcessStatus.FAILED);
    }

    //Generate a new TaskInfoBuilder class
    public static TaskInfoBuilder builder() {
        return new TaskInfoBuilder();
    }

    public byte[] getPayloadBytes() {
        return payloadBytes.clone();
    }

    public static class TaskInfoBuilder {
        @Nullable
        private Long id;
        @Nullable
        private UUID batchId;
        @Nullable
        private ProcessStatus status;
        @Nullable
        private Instant executionTime;
        @Nullable
        private byte[] payloadBytes;
        @Nullable
        private String handlerName;
        @Nullable
        private String partitionId;
        @Nullable
        private String objectType;
        @Nullable
        private Long version;
        @Nullable
        private Instant createdAt;
        @Nullable
        private Instant updatedAt;
        @Nullable
        private int retryCount;

        public TaskInfoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TaskInfoBuilder batchId(UUID batchId) {
            this.batchId = batchId;
            return this;
        }

        public TaskInfoBuilder status(ProcessStatus status) {
            this.status = status;
            return this;
        }

        public TaskInfoBuilder executionTime(Instant executionTime) {
            this.executionTime = executionTime;
            return this;
        }

        public TaskInfoBuilder payloadBytes(byte[] payloadBytes) {
            Assert.notNull(payloadBytes, "payloadBytes must not be null");
            this.payloadBytes = payloadBytes.clone();
            return this;
        }

        public TaskInfoBuilder handlerName(String handlerName) {
            this.handlerName = handlerName;
            return this;
        }

        public TaskInfoBuilder partitionId(String partitionId) {
            this.partitionId = partitionId;
            return this;
        }

        public TaskInfoBuilder objectType(String objectType) {
            this.objectType = objectType;
            return this;
        }

        public TaskInfoBuilder version(Long version) {
            this.version = version;
            return this;
        }

        public TaskInfoBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TaskInfoBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TaskInfoBuilder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public TaskInfo build() {
            return new TaskInfo(this);
        }
    }
}
