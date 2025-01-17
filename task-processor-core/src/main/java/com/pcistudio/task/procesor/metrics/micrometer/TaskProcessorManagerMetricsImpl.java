package com.pcistudio.task.procesor.metrics.micrometer;

import com.pcistudio.task.procesor.metrics.ManagerStats;
import com.pcistudio.task.procesor.metrics.TaskProcessorManagerMetrics;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.function.Supplier;

public class TaskProcessorManagerMetricsImpl extends TaskProcessorManagerMetrics {
    private final MeterRegistry meterRegistry;
    private final Supplier<ManagerStats> managerStatsSupplier;

    public TaskProcessorManagerMetricsImpl(MeterRegistry meterRegistry, Supplier<ManagerStats> managerStatsSupplier) {
        this.meterRegistry = meterRegistry;
        this.managerStatsSupplier = managerStatsSupplier;
        this.meterRegistry.gauge("task.processor.handler.count", this.managerStatsSupplier, value -> (double) value.get().getHandlersRegisteredCount());
        this.meterRegistry.gauge("task.processor.handler.running", this.managerStatsSupplier, value -> (double)value.get().getHandlersRunningCount());
        this.meterRegistry.gauge("task.processor.handler.paused", this.managerStatsSupplier, value -> (double)value.get().getHandlersPausedCount());
    }

}
