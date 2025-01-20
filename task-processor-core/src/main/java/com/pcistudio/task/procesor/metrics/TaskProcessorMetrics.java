package com.pcistudio.task.procesor.metrics;

import java.util.function.Supplier;

public interface TaskProcessorMetrics {

    void incrementTaskRequeueCount(int requeue);

    TimeMeter recordRequeueTime();

    TimeMeter recordTaskPolling();

    TimeMeter recordTaskProcess();

    void registerProcessor(Supplier<ProcessorStats> statsSupplier);
}
