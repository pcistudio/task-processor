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
public class RandomTaskHandler<T> implements TaskHandler<T> {
    private final static String SLOW_CALLS = "SLOW_CALLS";
    private final SecureRandom random = new SecureRandom();
    private final AtomicInteger numberOfCalls = new AtomicInteger(0);
    private final Map<Integer, ConsumerHolder<T>> exceptionsAndSlowIndexMap = new ConcurrentHashMap<>();

    private final Map<Class<? extends RuntimeException>, AtomicInteger> stats = new ConcurrentHashMap<>();

    private final AtomicInteger slowTaskCount = new AtomicInteger(0);

    private Clock clock;
    private Consumer<T> defaultConsumer;
    private int taskCount;

    private Instant startCalls;
    private boolean randomizeDuration = false;

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
            log.info("{} {}", aClass.getSimpleName(), integer.get());
        });
        log.info("{} {}", SLOW_CALLS, slowTaskCount.get());
    }

    public void assertExceptionCount(Class<? extends RuntimeException> exception, int count) {
        Assertions.assertThat(stats.getOrDefault(exception, new AtomicInteger(0)).get()).isEqualTo(count);
    }

    public void assertSlowCount(int count) {
        Assertions.assertThat(slowTaskCount.get()).isEqualTo(count);
    }

    public Builder builder() {
        return new RandomTaskHandler<T>().new Builder();
    }

    private static void simulateCall(int waitingTime) {
        if (waitingTime == 0) return;

        try {
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int getRandomDuration(int maxValue) {
        if (!randomizeDuration) {
            return 0;
        }
        return random.nextInt(maxValue);
    }

    public class Builder {
        private int taskCount;
        private int slowTaskCount = 0;

        private int slowTaskDurationMs = 20_000;
        private Duration stopSlowCallsAfter;
        private Duration stopErrorsCallsAfter;
        private Clock clock = Clock.systemDefaultZone();
        private boolean debugConfiguration = false;
        private final Map<RuntimeException, Integer> expectedExceptions = new HashMap<>();

        public Builder withTaskCount(int taskCount) {
            this.taskCount = taskCount;
            return this;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder withSlowTaskCount(int slowTaskCount) {
            this.slowTaskCount = slowTaskCount;
            return this;
        }

        public Builder withStopSlowCallsAfter(Duration stopSlowCallsAfter) {
            this.stopSlowCallsAfter = stopSlowCallsAfter;
            return this;
        }

        public Builder withStopErrorsCallsAfter(Duration stopErrorsCallsAfter) {
            this.stopErrorsCallsAfter = stopErrorsCallsAfter;
            return this;
        }

        public Builder withSlowTaskDurationMs(int slowTaskDurationMs) {
            this.slowTaskDurationMs = slowTaskDurationMs;
            return this;
        }

        public Builder withConsumer(Consumer<T> consumer) {
            RandomTaskHandler.this.defaultConsumer = consumer;
            return this;
        }

        public Builder withExpectedException(RuntimeException exception, int times) {
            Assert.isTrue(times > 0, "Times must be greater than 0");
            expectedExceptions.put(exception, times);
            return this;
        }

        public Builder enableRandomizeDurationCalls() {
            RandomTaskHandler.this.randomizeDuration = true;
            return this;
        }

        public Builder enableDebugConfiguration() {
            debugConfiguration = true;
            return this;
        }

        public RandomTaskHandler<T> build() {
            RandomTaskHandler.this.clock = this.clock;
            int totalExceptions = expectedExceptions.values().stream().mapToInt(Integer::intValue).sum();
            Assert.isTrue(totalExceptions + slowTaskCount <= taskCount, "Total exceptions plus slowTaskCount must be less than or equal to task count");

            shuffleExceptionsAndSlowCalls();

            if (debugConfiguration) {
                printExceptionsAndSlowIndexMap();
            }
            RandomTaskHandler.this.taskCount = taskCount;
            return RandomTaskHandler.this;
        }

        private void printExceptionsAndSlowIndexMap() {
            JsonUtil.print("-----------------Exception Config----------------\n", expectedExceptions);
            log.info("-----------------Randomized exceptions and slow calls index----------------");
            exceptionsAndSlowIndexMap.forEach((key, value) -> {
                if (value.isErrorConsumer()) {
                    log.info("{} => {}", key, value.getException().getClass().getSimpleName());
                } else {
                    log.info("{} => {}", key, SLOW_CALLS);
                }
            });
            log.info("--------------------------------------------------------------------------");
        }

        private void shuffleExceptionsAndSlowCalls() {
            List<ConsumerHolder<T>> list = new LinkedList<>();

            for (Map.Entry<RuntimeException, Integer> entry : expectedExceptions.entrySet()) {
                RuntimeException exception = entry.getKey();
                ConsumerHolder<T> exConsumer = ConsumerHolder.createErrorConsumer(getExceptionConsumer(exception), exception);
                for (int i = 0; i < entry.getValue(); i++) {
                    list.add(exConsumer);
                }
            }
            ConsumerHolder<T> slowConsumer = ConsumerHolder.createConsumer(getSlowConsumer());

            int slow = slowTaskCount;
            int success = taskCount - slowTaskCount - list.size();
            for (int i = 0; i < taskCount; i++) {
                int index = random.nextInt(list.size() + slow + success);
                if (index < list.size()) {
                    exceptionsAndSlowIndexMap.put(i, list.remove(index));
                } else if (index < list.size() + slow) {
                    slow--;
                    exceptionsAndSlowIndexMap.put(i, slowConsumer);
                } else {
                    success--;
                }
            }
        }

        private Consumer<T> getSlowConsumer() {
            return (t) -> {
                if (stopSlowCallsAfter != null && startCalls.plus(stopSlowCallsAfter).isBefore(clock.instant())) {
                    log.info("slow calls stopped");
                    defaultConsumer.accept(t);
                    return;
                }

                simulateCall(slowTaskDurationMs + getRandomDuration(2000));
                RandomTaskHandler.this.slowTaskCount.incrementAndGet();
            };
        }

        private Consumer<T> getExceptionConsumer(RuntimeException exception) {
            return (payload) -> {
                simulateCall(getRandomDuration(1000));
                if (stopErrorsCallsAfter != null && startCalls.plus(stopErrorsCallsAfter).isBefore(clock.instant())) {
                    log.info("All errors stopped");
                    defaultConsumer.accept(payload);
                    return;
                }
                throw exception;
            };
        }
    }

    private static class ConsumerHolder<T> implements Consumer<T> {
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
