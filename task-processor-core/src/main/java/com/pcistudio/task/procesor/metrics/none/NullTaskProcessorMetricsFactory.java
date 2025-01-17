package com.pcistudio.task.procesor.metrics.none;

import com.pcistudio.task.procesor.metrics.*;


import java.util.function.Supplier;

public class NullTaskProcessorMetricsFactory implements TaskProcessorMetricsFactory {

    @Override
    public TaskProcessorMetrics createHandlerMetrics(String handlerName) {
        return new TaskProcessorMetrics() {
            @Override
            public void incrementTaskRequeueCount(int requeue) {

            }

            @Override
            public TimeMeter recordRequeueTime() {
                return TimeMeter.EMPTY;
            }

            @Override
            public TimeMeter recordTaskPolling() {
                return TimeMeter.EMPTY;
            }

            @Override
            public TimeMeter recordTaskProcess() {
                return TimeMeter.EMPTY;
            }
        };
    }

    @Override
    public TaskProcessorManagerMetrics createManagerMetrics(Supplier<ManagerStats> managerStatsSupplier) {
        return new TaskProcessorManagerMetrics();
    }
}
