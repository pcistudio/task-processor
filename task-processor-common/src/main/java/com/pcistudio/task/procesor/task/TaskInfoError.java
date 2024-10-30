package com.pcistudio.task.procesor.task;


import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class TaskInfoError {
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private final Long taskId; // TODO could use a direct reference to TaskInfo

    /**
     * The partition id of the task. This is used to group tasks together. by the server that is processing them
     * whene an error happens we keep that record in the error
     * if the record is completed then the partitionId in the task will have the value of the server that processed the task
     */
    private String partitionId;
    private final String errorMessage;
    private String handlerName;
    private final Instant createdAt;

    public static final TaskInfoError EMPTY = TaskInfoError.builder().taskId(0L).errorMessage("").build();

    @Override
    public String toString() {
        return "TaskInfoError{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
