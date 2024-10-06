package com.pcistudio.task.procesor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HandlerPropertiesWrapper {
    private HandlerProperties delegate;

    public HandlerPropertiesWrapper(HandlerProperties delegate) {
        this.delegate = delegate;
    }

    public String getHandlerName() {
        return delegate.getHandlerName();
    }

    public String getHandlerClass() {
        return delegate.getHandlerClass();
    }

    public String getHandlerMethod() {
        return delegate.getHandlerMethod();
    }

    public int getMaxRetryCount() {
        return delegate.getMaxRetryCount();
    }

    public int getRetryIntervalMs() {
        return delegate.getRetryIntervalMs();
    }

    public int getMaxParallelTasks() {
        return delegate.getMaxParallelTasks();
    }

    public boolean isExponentialBackoff() {
        return delegate.isExponentialBackoff();
    }

    public String getTableName() {
        return "task_info_" + delegate.getTableName().trim();
    }

}
