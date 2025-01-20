package com.pcistudio.task.procesor.util;

public interface ExecutionTracker {
    void trackFuture(long taskId, Runnable runnable);

    void shutdown();

    long getTaskCount();

    long getLongWaitingTaskCount();
}