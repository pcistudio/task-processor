package com.pcistudio.task.procesor;


import lombok.Builder;
import lombok.Getter;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class TaskInfo<T> {
    private Long id;
    private UUID batchId;
    private ProcessStatus status;
    private String lastErrorMessage;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
    private T payload;
    private int retryCount;
    private String handlerName;
    private String objectType;

    public void incrementRetryCount() {
        retryCount++;
    }

    public void updateStatus(ProcessStatus status) {
        this.status = status;
    }

    public void updateLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
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
                ", lastErrorMessage='" + lastErrorMessage + '\'' +
                ", version=" + version +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", payload=" + payload +
                ", retryCount=" + retryCount +
                ", handlerName='" + handlerName + '\'' +
                ", objectType='" + objectType + '\'' +
                '}';
    }
}
