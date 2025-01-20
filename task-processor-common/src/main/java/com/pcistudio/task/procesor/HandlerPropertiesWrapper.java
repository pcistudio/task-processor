package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.handler.TaskHandler;
import lombok.Getter;

import javax.annotation.concurrent.Immutable;
import java.time.Duration;
import java.util.Set;

@Getter
@Immutable
public class HandlerPropertiesWrapper {
    private final HandlerProperties delegate;

    public HandlerPropertiesWrapper(HandlerProperties delegate) {
        this.delegate = delegate;
    }

    public String getHandlerName() {
        return delegate.getHandlerName();
    }

    public int getMaxRetries() {
        return delegate.getMaxRetries();
    }

    public int getRetryDelayMs() {
        return delegate.getRetryDelayMs();
    }

    public int getMaxParallelTasks() {
        return delegate.getMaxParallelTasks();
    }

    public boolean isExponentialBackoff() {
        return delegate.isExponentialBackoff();
    }

    public int getMaxPoll() {
        return delegate.getMaxPoll();
    }

    public long getPollInterval() {
        return delegate.getPollInterval();
    }

    public long getRequeueIntervalMs() {
        return delegate.getRequeueInterval();
    }

    public long getTaskExecutionTimeout() {
        return delegate.getTaskExecutionTimeout();
    }

    public long getLongTaskTimeMs() {
        return delegate.getLongTaskTimeMs();
    }

    public long getLongTaskCheckIntervalMs() {
        return delegate.getLongTaskCheckIntervalMs();
    }

    public long getLongTaskCheckInitialDelayMs() {
        return delegate.getLongTaskCheckInitialDelayMs();
    }


    public String getTableName() {
        return "task_info_" + delegate.getTableName().trim();
    }

    public Set<Class<? extends RuntimeException>> getTransientExceptions() {
        return delegate.getTransientExceptions();
    }

    public Duration getProcessingExpire() {
        return delegate.getProcessingExpire();
    }

    public Duration getProcessingGracePeriod() {
        return delegate.getProcessingGracePeriod();
    }

    public TaskHandler getTaskHandler() {
        return delegate.getTaskHandler();
    }

    public Class<?> getTaskHandlerType() {
        return delegate.getTaskHandlerType();
    }

    public boolean isAutoStartEnabled() {
        return delegate.isAutoStartEnabled();
    }
}
