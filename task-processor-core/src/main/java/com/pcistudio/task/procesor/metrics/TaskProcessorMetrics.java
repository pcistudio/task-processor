package com.pcistudio.task.procesor.metrics;

public interface TaskProcessorMetrics {
    void incrementTaskRequeueCount(int requeue);

    TimeMeter recordRequeueTime();

    TimeMeter recordTaskPolling();

    TimeMeter recordTaskProcess();
}
