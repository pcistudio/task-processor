package com.pcistudio.task.procesor;


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
    private String partition;
    private final String errorMessage;
    private String handlerName;
    @Builder.Default
    private final Instant createdAt = Instant.now();

    @Override
    public String toString() {
        return "TaskInfoError{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
