package com.pcistudio.task.procesor.util;

import java.time.Duration;
import java.time.Instant;

public interface ClockIncreaseStrategy {
    Instant currentInstant();

    void addTime(Duration duration);
}