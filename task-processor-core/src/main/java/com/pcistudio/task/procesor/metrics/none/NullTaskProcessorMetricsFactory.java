package com.pcistudio.task.procesor.metrics.none;

import com.pcistudio.task.procesor.metrics.*;
import lombok.NoArgsConstructor;


import java.util.function.Supplier;

@NoArgsConstructor
public class NullTaskProcessorMetricsFactory implements TaskProcessorMetricsFactory {

    @Override
    public TaskProcessorMetrics createHandlerMetrics(String handlerName) {
        return new NullTaskProcessorMetrics();
    }

    @Override
    public TaskProcessorManagerMetrics createManagerMetrics(Supplier<ManagerStats> statsSupplier) {
        return new NullTaskProcessorManagerMetrics();
    }

    private static final class NullTaskProcessorManagerMetrics implements TaskProcessorManagerMetrics {
    }

    private static final class NullTaskProcessorMetrics implements TaskProcessorMetrics {
        @Override
        public void incrementTaskRequeueCount(int requeue) {
            //Used when metrics are not been recorded
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

        @Override
        public void registerProcessor(Supplier<ProcessorStats> statsSupplier) {
            //Used when metrics are not been recorded
        }
    }
}
