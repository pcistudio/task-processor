package com.pcistudio.task.procesor.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A mutable clock that can be updated to simulate the passage of time.
 * should be used for testing purposes only. It was designed to avoid using Thread.sleep() in tests.
 * It can be used for concurrency but since it is locking real world scenarios will not happen.
 * <p>
 * This class is thread-safe.
 */
@Slf4j
public final class MutableFixedClock extends Clock {
    private final Clock baseClock;
    private boolean debug = false;
    private ClockIncreaseStrategy clockIncreaseStrategy = new FixedClockIncreaseStrategy();

    public MutableFixedClock(Clock baseClock) {
        Assert.notNull(baseClock, "baseClock cannot be null");
        Assert.isFalse(baseClock instanceof MutableFixedClock, "baseClock can not be an instance of MutableFixedClock");
        this.baseClock = baseClock;
        log.info("Clock initialized with: {}", baseClock.instant());
    }

    public MutableFixedClock(Instant instant, ZoneId zone) {
        this(Clock.fixed(instant, zone));
    }

    public MutableFixedClock(Instant instant) {
        this(instant, ZoneId.systemDefault());
    }

    public MutableFixedClock() {
        this(Instant.now());
    }

    public MutableFixedClock withDebug() {
        debug = true;
        return this;
    }

    public MutableFixedClock withRealTimeStrategy() {
        withRealTimeStrategy(new RealTimeClockIncreaseStrategy());
        return this;
    }

    public MutableFixedClock withRealTimeStrategy(ClockIncreaseStrategy clockIncreaseStrategy) {
        this.clockIncreaseStrategy = clockIncreaseStrategy;
        return this;
    }

    public void increaseTime(Duration duration) {
        clockIncreaseStrategy.addTime(duration);
    }

    @Override
    public Instant instant() {
        Instant instant = clockIncreaseStrategy.currentInstant();
        if (debug) {
            log.debug("Current time: {}", instant);
        }
        return instant;
    }

    @Override
    public long millis() {
        long epochMilli = clockIncreaseStrategy.currentInstant().toEpochMilli();
        if (debug) {
            log.debug("Current millis: {}", epochMilli);
        }
        return epochMilli;
    }

    @Override
    public ZoneId getZone() {
        return baseClock.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return baseClock.withZone(zone);
    }

    private class RealTimeClockIncreaseStrategy implements ClockIncreaseStrategy {
        private final long startTime = System.currentTimeMillis();
        private final AtomicLong timeAddedMillis = new AtomicLong(0);

        @Override
        public Instant currentInstant() {
            long timePass = System.currentTimeMillis() - startTime;
            return baseClock.instant().plusMillis(timePass + timeAddedMillis.get());
        }

        public void addTime(Duration duration) {
            timeAddedMillis.addAndGet(duration.toMillis());
        }
    }

    private class FixedClockIncreaseStrategy implements ClockIncreaseStrategy {
        private final AtomicLong timeAddedMillis = new AtomicLong(0);

        @Override
        public Instant currentInstant() {
            return baseClock.instant().plusMillis(timeAddedMillis.get());
        }

        public void addTime(Duration duration) {
            timeAddedMillis.addAndGet(duration.toMillis());
        }
    }
}
