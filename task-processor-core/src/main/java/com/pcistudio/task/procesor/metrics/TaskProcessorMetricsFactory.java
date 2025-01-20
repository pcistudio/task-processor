package com.pcistudio.task.procesor.metrics;

import java.util.function.Supplier;

public interface TaskProcessorMetricsFactory {
    TaskProcessorMetrics createHandlerMetrics(String handlerName);
    TaskProcessorManagerMetrics createManagerMetrics(Supplier<ManagerStats> statsSupplier);
}
