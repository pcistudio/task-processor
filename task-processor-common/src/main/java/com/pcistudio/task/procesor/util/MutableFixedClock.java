package com.pcistudio.task.procesor.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A mutable clock that can be updated to simulate the passage of time.
 * should be used for testing purposes only. It was designed to avoid using Thread.sleep in tests.
 * It can be use for concurrency but since it is locking real world scenarios will not happen.
 * <p>
 * This class is thread-safe.
 */
@Slf4j
public final class MutableFixedClock extends Clock {
    private Clock baseClock;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private boolean debug = false;

    public MutableFixedClock(Clock baseClock) {
        assertClock(baseClock);
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

    public void updateClock(Clock baseClock) {
        assertClock(baseClock);
        try {
            lock.writeLock().lock();
            this.baseClock = baseClock;
        } finally {
            lock.writeLock().unlock();
        }
        if (debug) {
            log.debug("Clock updated to: {}", baseClock.instant());
        }
    }

    public void increaseTime(Duration duration) {
        updateClock(Clock.offset(baseClock, duration));
    }

    private static void assertClock(Clock baseClock) {
        Assert.notNull(baseClock, "baseClock cannot be null");
        Assert.isFalse(baseClock instanceof MutableFixedClock, "baseClock can not be an instance of MutableFixedClock");
    }

    public MutableFixedClock debug() {
        debug = true;
        return this;
    }

    @Override
    public ZoneId getZone() {
        try {
            lock.readLock().lock();
            return baseClock.getZone();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Clock withZone(ZoneId zone) {
        updateClock(baseClock.withZone(zone));
        return this;
    }

    @Override
    public Instant instant() {
        try {
            lock.readLock().lock();
            if (debug) {
                log.debug("Current time: {}", baseClock.instant());
            }
            return baseClock.instant();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long millis() {
        if (debug) {
            log.debug("Current millis: {}", baseClock.millis());
        }
        return baseClock.millis();
    }
}
