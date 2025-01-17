package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.util.Assert;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;
import java.util.function.Consumer;

public class DefaultCircuitBreakerDecorator implements CircuitBreakerDecorator {

    private CircuitBreaker circuitBreaker;

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

    public DefaultCircuitBreakerDecorator(CircuitBreakerRegistry circuitBreakerRegistry) {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("task-run");
    }

    @Override
    public void addCircuitOpenListener(CircuitOpenListener circuitOpenListener) {
        Assert.notNull(circuitOpenListener, "circuitOpenListener can not be null");
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    if (event.getStateTransition().getToState() == CircuitBreaker.State.OPEN) {
                        circuitOpenListener.onOpen();
                    }
                });
    }

    @Override
    public <T> Consumer<T> decorate(Consumer<T> processTaskInfo) {
        return circuitBreaker.decorateConsumer(processTaskInfo);
    }

    @Override
    public void open() {
        circuitBreaker.transitionToOpenState();
    }
}
