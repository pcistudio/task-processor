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
    private String partitionId;
    private final String errorMessage;
    private String handlerName;
    @Builder.Default
    private final Instant createdAt = Instant.now();
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
