package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.util.Assert;
import com.pcistudio.task.procesor.util.decoder.MessageDecoding;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;

import java.time.Clock;
import java.util.Objects;
import java.util.Set;

public final class TaskProcessingContext {
    @Getter
    private final HandlerPropertiesWrapper handlerProperties;
    @Getter
    private final TaskHandler taskHandler;
    @Getter
    private final TaskInfoService taskInfoService;

    private final Set<Class<? extends RuntimeException>> transientErrors;
    @Getter
    private final RetryStrategy retryManager;
    @Getter
    private final MessageDecoding messageDecoding;
    /**
     * IF CircuitBreakerDecorator is not set, a DefaultCircuitBreakerDecorator will be use
     */
    @Getter
    private final CircuitBreakerDecorator circuitBreaker;
    @Getter
    private final Clock clock;
    @Getter
    private final Class<?> taskHandlerType;

    private TaskProcessingContext(Builder builder) {
//        Assert.notNull(builder.handlerProperties, "Handler properties cannot be null");
        if (builder.handlerProperties == null) {
            throw new IllegalArgumentException("Handler properties cannot be null");
        }
        Assert.notNull(builder.taskHandler, "taskHandler cannot be null");

        Assert.notNull(builder.taskInfoService, "taskInfoService cannot be null");
        Assert.notNull(builder.retryStrategy, "retryManager cannot be null");
        Assert.notNull(builder.messageDecoding, "messageDecoding cannot be null");
        Assert.notNull(builder.clock, "clock cannot be null");

        Set<Class<? extends RuntimeException>> transientExceptions = builder.handlerProperties.getTransientExceptions();
        transientExceptions.add(TaskHandlerTransientException.class);

        this.handlerProperties = builder.handlerProperties;
        this.taskHandler = builder.taskHandler;
        this.taskHandlerType = handlerProperties.getTaskHandlerType();
        this.taskInfoService = builder.taskInfoService;
        this.transientErrors = transientExceptions;
        this.retryManager = builder.retryStrategy;
        this.messageDecoding = builder.messageDecoding;
        this.circuitBreaker = Objects.requireNonNullElseGet(builder.circuitBreaker, DefaultCircuitBreakerDecorator::new);
        this.clock = builder.clock;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isTransient(final RuntimeException exception) {
        if (transientErrors.contains(exception.getClass())) {
            return true;
        }

        for (final Class<? extends RuntimeException> transientError : transientErrors) {
            if (transientError.isInstance(exception)) {
                transientErrors.add(exception.getClass());
                return true;
            }
        }
        return false;
    }


    public static class Builder {
        @Nullable
        private HandlerPropertiesWrapper handlerProperties;
        @Nullable
        private TaskHandler taskHandler;
        @Nullable
        private TaskInfoService taskInfoService;
        @Nullable
        private RetryStrategy retryStrategy;
        @Nullable
        private MessageDecoding messageDecoding;
        @Nullable
        private CircuitBreakerDecorator circuitBreaker;
        @Nullable
        private Clock clock;

        public Builder handlerProperties(final HandlerPropertiesWrapper handlerProperties) {
            this.handlerProperties = handlerProperties;
            return this;
        }

        public Builder taskHandler(final TaskHandler taskHandler) {
            this.taskHandler = taskHandler;
            return this;
        }

        public Builder taskInfoService(final TaskInfoService taskInfoService) {
            this.taskInfoService = taskInfoService;
            return this;
        }

        public Builder retryManager(final RetryStrategy retryStrategy) {
            this.retryStrategy = retryStrategy;
            return this;
        }

        public Builder messageDecoding(final MessageDecoding messageDecoding) {
            this.messageDecoding = messageDecoding;
            return this;
        }

        public Builder circuitBreakerDecorator(final @Nullable CircuitBreakerDecorator circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
            return this;
        }

        public Builder clock(final Clock clock) {
            this.clock = clock;
            return this;
        }

        public TaskProcessingContext build() {
            return new TaskProcessingContext(this);
        }
    }
}
