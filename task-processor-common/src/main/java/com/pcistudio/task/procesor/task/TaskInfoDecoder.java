package com.pcistudio.task.procesor.task;


import com.pcistudio.task.procesor.util.decoder.MessageDecoding;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Wrapper class that deal with the payload decoding
 */
@RequiredArgsConstructor
public class TaskInfoDecoder implements TaskInfoOperations, TaskInfoPayloadObject {
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final TaskInfo delegate;
    private final MessageDecoding messageDecoding;
    private final Class<?> objectType;
    /**
     * It is decoded the payload object only once
     */
    @Nullable
    private Object payload;

    @Override
    @Nullable
    public Long getId() {
        return delegate.getId();
    }

    @Override
    @Nullable
    public UUID getBatchId() {
        return delegate.getBatchId();
    }

    @Override
    public ProcessStatus getStatus() {
        return delegate.getStatus();
    }

    @Override
    public Long getVersion() {
        return delegate.getVersion();
    }

    @Override
    @Nullable
    public Instant getCreatedAt() {
        return delegate.getCreatedAt();
    }

    @Override
    @Nullable
    public Instant getUpdatedAt() {
        return delegate.getUpdatedAt();
    }

    @Override
    public Instant getExecutionTime() {
        return delegate.getExecutionTime();
    }

    @Override
    public int getRetryCount() {
        return delegate.getRetryCount();
    }

    @Override
    public String getHandlerName() {
        return delegate.getHandlerName();
    }

    @Override
    @Nullable
    public String getPartitionId() {
        return delegate.getPartitionId();
    }

    @Override
    @Nullable
    public String getObjectType() {
        return delegate.getObjectType();
    }

    @Override
    public Object getPayload() {
        if (payload == null) {
            payload = messageDecoding.decode(delegate.getPayloadBytes(), objectType);
        }
        return payload;
    }

    @Override
    public void completed() {
        delegate.completed();
    }

    @Override
    public void markForRetry() {
        delegate.markForRetry();
    }

    @Override
    public void failed() {
        delegate.failed();
    }
}
