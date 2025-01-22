package com.pcistudio.task.procesor.task;


import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public final class TaskInfoError {
    @Nullable
    private UUID id;
    private final Long taskId; // TODO could use a direct reference to TaskInfo

    /**
     * The partition id of the task. This is used to group tasks together. by the server that is processing them
     * when an error happens we keep that record in the error
     * if the record is completed then the partitionId in the task will have the value of the server that processed the task
     */
    private final String partitionId;
    private final String errorMessage;
    @Nullable
    private final String handlerName;
    @Nullable
    private final Instant createdAt;

    public TaskInfoError(TaskInfoErrorBuilder builder) {
        this.id = Objects.requireNonNullElseGet(builder.id, UUID::randomUUID);
        this.taskId = Objects.requireNonNull(builder.taskId, "taskId must not be null");
        this.partitionId = Objects.requireNonNull(builder.partitionId, "partitionId must not be null");
        this.errorMessage = Objects.requireNonNull(builder.errorMessage, "errorMessage must not be null");
        this.handlerName = builder.handlerName;
        this.createdAt = builder.createdAt;
    }

    public static TaskInfoErrorBuilder builder() {
        return new TaskInfoErrorBuilder();
    }

    public static final class TaskInfoErrorBuilder {
        @Nullable
        private UUID id;
        @Nullable
        private Long taskId; // TODO could use a direct reference to TaskInfo
        @Nullable
        private String partitionId;
        @Nullable
        private String errorMessage;
        @Nullable
        private String handlerName;
        @Nullable
        private Instant createdAt;

        private TaskInfoErrorBuilder() {
        }

        public TaskInfoErrorBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TaskInfoErrorBuilder taskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public TaskInfoErrorBuilder partitionId(String partitionId) {
            this.partitionId = partitionId;
            return this;
        }

        public TaskInfoErrorBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public TaskInfoErrorBuilder handlerName(String handlerName) {
            this.handlerName = handlerName;
            return this;
        }

        public TaskInfoErrorBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TaskInfoError build() {
            return new TaskInfoError(this);
        }
    }

    public static final TaskInfoError EMPTY = builder()
            .taskId(0L).errorMessage("").partitionId("")
            .handlerName("").createdAt(Instant.MIN)
            .build();

    @Override
    public String toString() {
        return "TaskInfoError{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
