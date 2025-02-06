package com.pcistudio.task.procesor.handler;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@RequiredArgsConstructor
public class ExponentialRetryStrategy implements RetryStrategy {
    private final long retryDelayMs;
    private final long maxRetries;
    private final Clock clock;

    /**
     * Get the next retry time based on the retryDelayMs and the retryCount. The next retry time is calculated as exponential backoff.
     * This value will be place in the database and used to determine when to retry the task.
     * @param retryCount the current retry count
     * @return the next retry time.
     */
    @Override
    public Instant nextRetry(int retryCount) {
        return Instant.now(clock).plusMillis(retryDelayMs * (long) Math.pow(2, retryCount));
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
