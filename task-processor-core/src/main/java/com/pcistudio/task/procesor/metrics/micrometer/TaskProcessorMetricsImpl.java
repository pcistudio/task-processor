package com.pcistudio.task.procesor.metrics.micrometer;

import com.pcistudio.task.procesor.metrics.ProcessorStats;
import com.pcistudio.task.procesor.metrics.TaskProcessorMetrics;
import com.pcistudio.task.procesor.metrics.TimeMeter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import java.util.function.Supplier;

@SuppressFBWarnings({"EI_EXPOSE_REP2"})
public class TaskProcessorMetricsImpl implements TaskProcessorMetrics {
    private final MeterRegistry meterRegistry;
    private final String handlerName;
    private final Tags commonTag;
    private final Counter requeueCounter;

    public TaskProcessorMetricsImpl(final String handlerName, final MeterRegistry meterRegistry) {
        this.handlerName = handlerName;
        this.meterRegistry = meterRegistry;
        this.commonTag = Tags.of(
                "handler", handlerName
        );
        requeueCounter = meterRegistry.counter("task.processor.handler.requeue", commonTag);
    }

    @Override
    public void incrementTaskRequeueCount(final int requeue) {
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
    public void registerProcessor(Supplier<ProcessorStats> statsSupplier) {
        meterRegistry.gauge("task.processor.track.executions", commonTag, statsSupplier, value -> value.get().getTrackedTaskCount());
        meterRegistry.gauge("task.processor.track.long.executions", commonTag, statsSupplier, value -> value.get().getTrackedLongWaitingTaskCount());
        meterRegistry.gauge("task.processor.circuitbreaker.closed", commonTag, statsSupplier, value -> value.get().getCircuitBreakerClosed());
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

        public TimeMeterIml(final String indicator, final Tags tags) {
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
                            ERROR_TAG, "none",
                            OUTCOME_TAG, "success"
                    )
            ));
        }

        @Override
        public void error(final Throwable exception) {
            sample.stop(meterRegistry.timer(
                    indicator,
                    Tags.concat(
                            tags,
                            ERROR_TAG, exception.getClass().getSimpleName(),
                            OUTCOME_TAG, "error"
                    )
            ));
        }

        @Override
        public void retry(final Throwable exception) {
            sample.stop(meterRegistry.timer(
                    indicator,
                    Tags.concat(
                            tags,
                            ERROR_TAG, exception.getClass().getSimpleName(),
                            OUTCOME_TAG, "retry"
                    )
            ));
        }
    }

}
