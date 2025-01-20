package com.pcistudio.task.procesor.metrics.micrometer;

import com.pcistudio.task.procesor.metrics.ManagerStats;
import com.pcistudio.task.procesor.metrics.TaskProcessorManagerMetrics;
import com.pcistudio.task.procesor.metrics.TaskProcessorMetricsFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
@SuppressFBWarnings("EI_EXPOSE_REP")
public class DefaultTaskProcessorMetricsFactory implements TaskProcessorMetricsFactory {
    private final MeterRegistry meterRegistry;

    @Override
    public TaskProcessorMetricsImpl createHandlerMetrics(final String handlerName) {
        return new TaskProcessorMetricsImpl(handlerName, meterRegistry);
    }

    @Override
    public TaskProcessorManagerMetrics createManagerMetrics(final Supplier<ManagerStats> statsSupplier) {
        return new TaskProcessorManagerMetricsImpl(meterRegistry, statsSupplier);
    }
}
