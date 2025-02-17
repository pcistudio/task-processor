package com.pcistudio.processor.test.handler;

import com.pcistudio.task.procesor.handler.TaskHandler;
import com.pcistudio.task.procesor.util.Assert;
import com.pcistudio.task.procesor.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
public final class RandomTaskHandler<T> implements TaskHandler<T> {
    private static final String SLOW_CALLS = "SLOW_CALLS";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final AtomicInteger numberOfCalls = new AtomicInteger(0);
    private final Map<Integer, ConsumerHolder<T>> exceptionsAndSlowIndexMap;

    private final Map<Class<? extends RuntimeException>, AtomicInteger> stats = new ConcurrentHashMap<>();

    private final AtomicInteger slowTaskCount = new AtomicInteger(0);

    private final Clock clock;
    private final Consumer<T> defaultConsumer;
    private final int taskCount;

    /**
     * Duration to be considered slow task in milliseconds
     */
    private final int slowTaskDurationMs;
    private final Duration stopSlowCallsAfter;
    private final Duration stopErrorsCallsAfter;


    private Instant startCalls;
    private boolean randomizeDuration = false;


    private RandomTaskHandler(RandomTaskHandlerBuilder<T> builder) {
        this.clock = builder.clock;
        this.defaultConsumer = builder.defaultConsumer == null ? t -> {
        } : builder.defaultConsumer;
        this.taskCount = builder.taskCount;
        this.randomizeDuration = builder.randomizeDuration;
        this.slowTaskDurationMs = builder.slowTaskDurationMs;
        this.stopSlowCallsAfter = builder.stopSlowCallsAfter;
        this.stopErrorsCallsAfter = builder.stopErrorsCallsAfter;

        exceptionsAndSlowIndexMap = new ConcurrentHashMap<>(shuffleExceptionsAndSlowCalls(builder, this));

        if (builder.debugConfiguration) {
            printExceptionsAndSlowIndexMap(builder.expectedExceptions, exceptionsAndSlowIndexMap);
        }
    }


    @Override
    public void process(T payload) {
        initiateCalls();
        int callIndex = numberOfCalls.getAndIncrement() % taskCount;

        if (exceptionsAndSlowIndexMap.containsKey(callIndex)) {
            try {
                // exception and slow calls
                exceptionsAndSlowIndexMap.get(callIndex).accept(payload);
                return;
            } catch (RuntimeException e) {
                stats.putIfAbsent(e.getClass(), new AtomicInteger(0));
                stats.get(e.getClass()).incrementAndGet();
                throw e;
            }
        }

        simulateCall(1000);

        if (defaultConsumer != null) {
            defaultConsumer.accept(payload);
        }
    }

    private void initiateCalls() {
        if (startCalls == null) {
            startCalls = clock.instant();
        }
    }

    public void reset() {
        startCalls = null;
        numberOfCalls.set(0);
        stats.clear();
    }

    public void printStats() {
        stats.forEach((aClass, integer) -> {
            if (log.isInfoEnabled()) {
                log.info("{} {}", aClass.getSimpleName(), integer.get());
            }
        });
        if (log.isInfoEnabled()) {
            log.info("{} {}", SLOW_CALLS, slowTaskCount.get());
        }
    }

    public void assertExceptionCount(Class<? extends RuntimeException> exception, int count) {
        Assertions.assertThat(stats.getOrDefault(exception, new AtomicInteger(0)).get()).isEqualTo(count);
    }

    public void assertSlowCount(int count) {
        Assertions.assertThat(slowTaskCount.get()).isEqualTo(count);
    }

    public static <T> RandomTaskHandlerBuilder<T> builder() {
        return new RandomTaskHandlerBuilder<>();
    }

    private static void simulateCall(int waitingTime) {
        if (waitingTime == 0) {
            return;
        }

        try {
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void printExceptionsAndSlowIndexMap(Map<RuntimeException, Integer> expectedExceptions, Map<Integer, ConsumerHolder<T>> exceptionsAndSlowIndexMap) {
        JsonUtil.print("-----------------Exception Config----------------\n", expectedExceptions);
        log.info("-----------------Randomized exceptions and slow calls index----------------");

        exceptionsAndSlowIndexMap.forEach((key, value) -> {
            if (value.isErrorConsumer()) {
                log.info("{} => {}", key, value.getException().getClass().getSimpleName());//NOPMD
            } else {
                log.info("{} => {}", key, SLOW_CALLS);
            }
        });
        log.info("--------------------------------------------------------------------------");
    }

    private static <T> Map<Integer, ConsumerHolder<T>> shuffleExceptionsAndSlowCalls(RandomTaskHandlerBuilder<T> builder, RandomTaskHandler<T> taskHandler) {
        List<ConsumerHolder<T>> list = new LinkedList<>();

        for (Map.Entry<RuntimeException, Integer> entry : builder.expectedExceptions.entrySet()) {
            RuntimeException exception = entry.getKey();
            ConsumerHolder<T> exConsumer = ConsumerHolder.createErrorConsumer(t -> taskHandler.runException(t, exception), exception);
            for (int i = 0; i < entry.getValue(); i++) {
                list.add(exConsumer);
            }
        }
        ConsumerHolder<T> slowConsumer = ConsumerHolder.createConsumer(taskHandler::runSlow);
        int slow = builder.slowTaskCount;
        int success = builder.taskCount - builder.slowTaskCount - list.size();
        Map<Integer, ConsumerHolder<T>> exceptionsAndSlowIndexMap = new HashMap<>();
        for (int i = 0; i < builder.taskCount; i++) {
            int index = RANDOM.nextInt(list.size() + slow + success);
            if (index < list.size()) {
                exceptionsAndSlowIndexMap.put(i, list.remove(index));
            } else if (index < list.size() + slow) {
                slow--;
                exceptionsAndSlowIndexMap.put(i, slowConsumer);
            } else {
                success--;
            }
        }

        return exceptionsAndSlowIndexMap;
    }

    private void runSlow(T payload) {
        if (stopSlowCallsAfter != null && startCalls.plus(stopSlowCallsAfter).isBefore(clock.instant())) {
            log.info("slow calls stopped");
            defaultConsumer.accept(payload);
            return;
        }

        simulateCall(slowTaskDurationMs + getRandomDuration(2000));
        slowTaskCount.incrementAndGet();
    }

    private void runException(T payload, RuntimeException exception) {
        simulateCall(getRandomDuration(1000));
        if (stopErrorsCallsAfter != null && startCalls.plus(stopErrorsCallsAfter).isBefore(clock.instant())) {
            log.info("All errors stopped");
            defaultConsumer.accept(payload);
            return;
        }
        throw exception;
    }

    private int getRandomDuration(int maxValue) {
        if (!randomizeDuration) {
            return 0;
        }
        return RANDOM.nextInt(maxValue);
    }

    public static class RandomTaskHandlerBuilder<T> {
        private int taskCount;
        private int slowTaskCount = 0;

        private int slowTaskDurationMs = 20_000;
        private Duration stopSlowCallsAfter;
        private Duration stopErrorsCallsAfter;
        private Clock clock = Clock.systemDefaultZone();
        private boolean debugConfiguration = false;
        private final Map<RuntimeException, Integer> expectedExceptions = new HashMap<>();
        private Consumer<T> defaultConsumer;
        private boolean randomizeDuration = false;

        public RandomTaskHandlerBuilder<T> withTaskCount(int taskCount) {
            this.taskCount = taskCount;
            return this;
        }

        public RandomTaskHandlerBuilder<T> withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public RandomTaskHandlerBuilder<T> withSlowTaskCount(int slowTaskCount) {
            this.slowTaskCount = slowTaskCount;
            return this;
        }

        public RandomTaskHandlerBuilder<T> withStopSlowCallsAfter(Duration stopSlowCallsAfter) {
            this.stopSlowCallsAfter = stopSlowCallsAfter;
            return this;
        }

        public RandomTaskHandlerBuilder<T> withStopErrorsCallsAfter(Duration stopErrorsCallsAfter) {
            this.stopErrorsCallsAfter = stopErrorsCallsAfter;
            return this;
        }

        public RandomTaskHandlerBuilder<T> withSlowTaskDurationMs(int slowTaskDurationMs) {
            this.slowTaskDurationMs = slowTaskDurationMs;
            return this;
        }

        public RandomTaskHandlerBuilder<T> withConsumer(Consumer<T> consumer) {
            this.defaultConsumer = consumer;
            return this;
        }

        public RandomTaskHandlerBuilder<T> withExpectedException(RuntimeException exception, int times) {
            Assert.isTrue(times > 0, "Times must be greater than 0");
            expectedExceptions.put(exception, times);
            return this;
        }

        public RandomTaskHandlerBuilder<T> enableRandomizeDurationCalls() {
            this.randomizeDuration = true;
            return this;
        }

        public RandomTaskHandlerBuilder<T> enableDebugConfiguration() {
            this.debugConfiguration = true;
            return this;
        }

        public RandomTaskHandler<T> build() {
            int totalExceptions = expectedExceptions.values().stream().mapToInt(Integer::intValue).sum();
            Assert.isTrue(totalExceptions + slowTaskCount <= taskCount, "Total exceptions plus slowTaskCount must be less than or equal to task count");

            return new RandomTaskHandler<>(this);

        }


    }

    private static final class ConsumerHolder<T> implements Consumer<T> {
        private final Consumer<T> delegate;
        private final RuntimeException exception;

        private ConsumerHolder(Consumer<T> consumer, RuntimeException exception) {
            this.delegate = consumer;
            this.exception = exception;
        }

        public static <T> ConsumerHolder<T> createConsumer(Consumer<T> consumer) {
            return new ConsumerHolder<>(consumer, null);
        }

        public static <T> ConsumerHolder<T> createErrorConsumer(Consumer<T> consumer, RuntimeException exception) {
            return new ConsumerHolder<>(consumer, exception);
        }

        public boolean isErrorConsumer() {
            return exception != null;
        }

        public RuntimeException getException() {
            return exception;
        }

        @Override
        public void accept(T t) {
            delegate.accept(t);
        }
    }
}
