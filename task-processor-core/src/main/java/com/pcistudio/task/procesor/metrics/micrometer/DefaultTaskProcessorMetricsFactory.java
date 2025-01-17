package com.pcistudio.task.procesor.metrics.micrometer;

import com.pcistudio.task.procesor.metrics.ManagerStats;
import com.pcistudio.task.procesor.metrics.TaskProcessorManagerMetrics;
import com.pcistudio.task.procesor.metrics.TaskProcessorMetricsFactory;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class DefaultTaskProcessorMetricsFactory implements TaskProcessorMetricsFactory {
    private final MeterRegistry meterRegistry;

    @Override
    public TaskProcessorMetricsImpl createHandlerMetrics(String handlerName) {
        return new TaskProcessorMetricsImpl(handlerName, meterRegistry);
    }

    @Override
    public TaskProcessorManagerMetrics createManagerMetrics(Supplier<ManagerStats> managerStatsSupplier) {
        return new TaskProcessorManagerMetricsImpl(meterRegistry, managerStatsSupplier);
    }
}
