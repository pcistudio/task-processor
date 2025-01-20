package com.pcistudio.task.procesor.handler;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;
import java.util.function.Consumer;

public class DefaultCircuitBreakerDecorator implements CircuitBreakerDecorator {

    private final CircuitBreaker circuitBreaker;

    public DefaultCircuitBreakerDecorator() {
        this(
                CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)                        // Set failure threshold to 50%
                        .slowCallRateThreshold(50)                       // Set slow call threshold to 50%
                        .waitDurationInOpenState(Duration.ofSeconds(60)) // Wait 60s before transitioning from OPEN to HALF_OPEN
                        .slowCallDurationThreshold(Duration.ofSeconds(60))// Consider calls slower than 2s as "slow"
                        .permittedNumberOfCallsInHalfOpenState(5)        // Allow 5 calls when in HALF_OPEN state
                        .minimumNumberOfCalls(10)                        // Require at least 10 calls to calculate failure rate
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // Use COUNT_BASED or TIME_BASED window
                        .slidingWindowSize(20)                           // Window size for recording call metrics
                        .build())
        );
    }

    public DefaultCircuitBreakerDecorator(final CircuitBreakerRegistry registry) {
        circuitBreaker = registry.circuitBreaker("task-run");
    }

    @Override
    public <T> Consumer<T> decorate(final Consumer<T> processTaskInfo) {
        return circuitBreaker.decorateConsumer(processTaskInfo);
    }

    @Override
    public boolean isClosed() {
        return circuitBreaker.getState().equals(CircuitBreaker.State.CLOSED);
    }
}
