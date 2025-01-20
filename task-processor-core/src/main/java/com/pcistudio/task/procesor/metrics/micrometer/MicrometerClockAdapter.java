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
        final long seconds = clock.instant().getEpochSecond();
        final long nanos = clock.instant().getNano();
        if (seconds < 0 && nanos > 0) {
            final long nano = Math.multiplyExact(seconds + 1, 1_000_000_000L);
            final long adjustment = nanos - 1_000_000_000L;
            return Math.addExact(nano, adjustment);
        } else {
            final long nano = Math.multiplyExact(seconds, 1_000_000_000L);
            return Math.addExact(nano, nanos);
        }
    }

    public static MicrometerClockAdapter of(final Clock clock) {
        return new MicrometerClockAdapter(clock);
    }
}
