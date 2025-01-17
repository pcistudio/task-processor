package com.pcistudio.task.procesor.metrics.micrometer;

import com.pcistudio.task.procesor.metrics.TaskProcessorMetrics;
import com.pcistudio.task.procesor.metrics.TimeMeter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

public class TaskProcessorMetricsImpl implements TaskProcessorMetrics {
    private final MeterRegistry meterRegistry;
    private final String handlerName;
    private final Tags commonTag;
    private final Counter requeueCounter;

    public TaskProcessorMetricsImpl(String handlerName, MeterRegistry meterRegistry) {
        this.handlerName = handlerName;
        this.meterRegistry = meterRegistry;
        this.commonTag = Tags.of(
                "handler", handlerName
        );
        requeueCounter = meterRegistry.counter("task.processor.handler.requeue", commonTag);
    }

    @Override
    public void incrementTaskRequeueCount(int requeue) {
        requeueCounter.increment(requeue);
    }

    @Override
    public TimeMeter recordRequeueTime() {
        return new TimeMeterIml("task.processor.handler.requeue.seconds", commonTag);
    }

    @Override
    public TimeMeter recordTaskPolling() {
        return new TimeMeterIml("task.processor.handler.polling.seconds", commonTag);
    }

    @Override
    public TimeMeter recordTaskProcess() {
        return new TimeMeterIml("task.processor.handler.processing.seconds", commonTag);
    }

    @Override
    public String toString() {
        return "TaskProcessorMetrics{" +
                "handlerName='" + handlerName + '\'' +
                '}';
    }

    public class TimeMeterIml implements TimeMeter {
        private Timer.Sample sample;
        private final String indicator;
        private final Tags tags;

        public TimeMeterIml(String indicator, Tags tags) {
            sample = Timer.start();
            this.indicator = indicator;
            this.tags = tags;
        }

        @Override
        public void success() {
            sample.stop(meterRegistry.timer(
                    indicator,
                    Tags.concat(
                            tags,
                            "exception", "none",
                            "outcome", "success"
                    )
            ));
        }

        @Override
        public void error(Throwable exception) {
            sample.stop(meterRegistry.timer(
                    indicator,
                    Tags.concat(
                            tags,
                            "exception", exception.getClass().getSimpleName(),
                            "outcome", "error"
                    )
            ));
        }

        @Override
        public void retry(Throwable exception) {
            sample.stop(meterRegistry.timer(
                    indicator,
                    Tags.concat(
                            tags,
                            "exception", exception.getClass().getSimpleName(),
                            "outcome", "retry"
                    )
            ));
        }
    }

}
