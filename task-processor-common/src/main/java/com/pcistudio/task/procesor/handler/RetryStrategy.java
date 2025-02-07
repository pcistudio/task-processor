package com.pcistudio.task.procesor.handler;

import java.time.Instant;

public interface RetryStrategy {
    /**
     * Get the next retry time. This value will be place in the database and used to determine when to retry the task.
     * @param retryCount the current retry count
     * @return the next retry time.
     */
    Instant nextRetry(int retryCount);
    /**
     * Determine if the task should be retried.
     * @param retryCount the current retry count
     * @return true if the task should be retried, false otherwise.
     */
    boolean shouldRetry(int retryCount);
}
