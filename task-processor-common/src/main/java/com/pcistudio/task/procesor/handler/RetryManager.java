package com.pcistudio.task.procesor.handler;

import java.time.Instant;

public interface RetryManager {
    Instant nextRetry(int retryCount);
    boolean shouldRetry(int retryCount);
}
