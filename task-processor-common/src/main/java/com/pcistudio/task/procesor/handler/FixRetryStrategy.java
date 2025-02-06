package com.pcistudio.task.procesor.handler;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@RequiredArgsConstructor
public class FixRetryStrategy implements RetryStrategy {
    private final long retryDelayMs;
    private final long maxRetries;
    private final Clock clock;

    /**
     * Get the next retry time based on the retryDelayMs and the retryCount. The next retry time is calculated as a fixed delay.
     * @param retryCount the current retry count
     * @return
     */
    @Override
    public Instant nextRetry(int retryCount) {
        return Instant.now(clock).plusMillis(retryDelayMs);
    }

    /**
     * Determine if the task should be retried based on the maxRetries.
     * @param retryCount the current retry count
     * @return true if the task should be retried, false otherwise.
     */
    @Override
    public boolean shouldRetry(int retryCount) {
        return retryCount < maxRetries;
    }
}
