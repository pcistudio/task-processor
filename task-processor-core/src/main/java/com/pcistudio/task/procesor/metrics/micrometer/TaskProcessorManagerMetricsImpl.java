package com.pcistudio.task.procesor.metrics.micrometer;

import com.pcistudio.task.procesor.metrics.ManagerStats;
import com.pcistudio.task.procesor.metrics.TaskProcessorManagerMetrics;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.function.Supplier;

@SuppressFBWarnings({"EI_EXPOSE_REP2"})
public class TaskProcessorManagerMetricsImpl implements TaskProcessorManagerMetrics {
    private final MeterRegistry meterRegistry;

    public TaskProcessorManagerMetricsImpl(final MeterRegistry meterRegistry, final Supplier<ManagerStats> statsSupplier) {
        super();
        this.meterRegistry = meterRegistry;
        this.meterRegistry.gauge("task.processor.handler.count", statsSupplier, value -> value.get().getTotalHandlers());
        this.meterRegistry.gauge("task.processor.handler.running", statsSupplier, value -> value.get().getRunningHandlers());
        this.meterRegistry.gauge("task.processor.handler.paused", statsSupplier, value -> value.get().getPausedHandlers());
    }

}
