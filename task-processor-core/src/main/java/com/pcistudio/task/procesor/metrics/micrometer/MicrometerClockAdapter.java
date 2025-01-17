package com.pcistudio.task.procesor.metrics.micrometer;

import lombok.RequiredArgsConstructor;

import java.time.Clock;

@RequiredArgsConstructor
public class MicrometerClockAdapter implements io.micrometer.core.instrument.Clock {

    private final Clock clock;

    @Override
    public long wallTime() {
        return clock.millis();
    }

    @Override
    public long monotonicTime() {
        long seconds = clock.instant().getEpochSecond();
        long nanos = clock.instant().getNano();
        if (seconds < 0 && nanos > 0) {
            long nano = Math.multiplyExact(seconds + 1, 1000_000_000L);
            long adjustment = nanos - 1000_000_000L;
            return Math.addExact(nano, adjustment);
        } else {
            long nano = Math.multiplyExact(seconds, 1000_000_000L);
            return Math.addExact(nano, nanos);
        }
    }

    public static MicrometerClockAdapter of(Clock clock) {
        return new MicrometerClockAdapter(clock);
    }
}
