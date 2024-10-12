package com.pcistudio.task.procesor.handler;

import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class FixRetryManager implements RetryManager {
    private final long retryDelayMs;
    private final long maxRetries;

    public Instant nextRetry(int retryCount) {
        return Instant.now().plusMillis(retryDelayMs);
    }

    @Override
    public boolean shouldRetry(int retryCount) {
        return retryCount < maxRetries;
    }
}
