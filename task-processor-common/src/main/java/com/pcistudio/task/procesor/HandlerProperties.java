package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.handler.TaskHandler;
import com.pcistudio.task.procesor.util.Assert;
import com.pcistudio.task.procesor.util.GenericTypeUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;

import javax.annotation.concurrent.Immutable;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Immutable
public final class HandlerProperties extends HandlerWriteProperties {

    private final int maxRetries;

    private final int retryDelayMs;

    private final boolean exponentialBackoff;
    /**
     * Maximum number of tasks to poll from the queue
     */
    private final int maxPoll;

    private final int pollInterval;


    private final int requeueInterval;

    /**
     * Maximum time a task can run before being considered a long task
     */
    private final int longTaskTimeMs;
    /**
     * Interval to check for long running tasks
     */
    private final int longTaskCheckIntervalMs;
    /**
     * Initial delay to check for long running tasks
     */
    private final int longTaskCheckInitialDelayMs;

    private final int maxParallelTasks;

    private final Set<Class<? extends RuntimeException>> transientExceptions;

    private final Duration processingExpire;

    private final Duration processingGracePeriod;
    /**
     * <p>Task handler represent the action to be executed for the task to complete.</p>
     * <p>
     * taskHandler value should be <code>@Nullable</code> because in the case that Only the producer(writer) is configured,
     * the taskHandler is NOT NEEDED, but still need to create the table for the task if it does not exist.
     */
    @Nullable
    private final TaskHandler taskHandler;
    @Nullable
    private final Class<?> taskHandlerType;

    private final boolean autoStartEnabled;

    private HandlerProperties(HandlerPropertiesBuilder builder) {
        super(builder);
        this.maxRetries = builder.maxRetries;
        this.retryDelayMs = builder.retryDelayMs;
        this.exponentialBackoff = builder.exponentialBackoff;
        this.maxPoll = builder.maxPoll;
        this.pollInterval = builder.pollInterval;
        this.requeueInterval = builder.requeueInterval;

        this.longTaskTimeMs = builder.longTaskTimeMs;
        this.longTaskCheckIntervalMs = builder.longTaskCheckIntervalMs;
        this.longTaskCheckInitialDelayMs = builder.longTaskCheckInitialDelayMs;
        this.maxParallelTasks = builder.maxParallelTasks;
        this.processingExpire = builder.processingExpire;
        this.processingGracePeriod = builder.processingGracePeriod;
        this.taskHandler = builder.taskHandler;
        this.taskHandlerType = builder.taskHandlerType;
        this.autoStartEnabled = builder.autoStartEnabled;
        this.transientExceptions = Objects.requireNonNullElse(builder.transientExceptions, Collections.emptySet());
    }

    public static HandlerPropertiesBuilder builder() {
        return new HandlerPropertiesBuilder();
    }

    public Set<Class<? extends RuntimeException>> getTransientExceptions() {
        return new HashSet<>(transientExceptions);
    }

    public static class HandlerPropertiesBuilder extends HandlerWritePropertiesBuilder<HandlerPropertiesBuilder> {
        private int maxRetries = 3;
        private int retryDelayMs = 1000;
        private boolean exponentialBackoff = false;
        private int maxPoll = 16;
        private int pollInterval = 60000;
        private int requeueInterval = 300000;
        private int longTaskTimeMs = 180000;
        private int longTaskCheckIntervalMs = 60000;
        private int longTaskCheckInitialDelayMs = 60000;
        private int maxParallelTasks = 1;
        private Duration processingExpire = Duration.ofMinutes(5);
        private Duration processingGracePeriod = Duration.ofMinutes(0);
        @Nullable
        private TaskHandler taskHandler;
        @Nullable
        private Class<?> taskHandlerType;
        private boolean autoStartEnabled = true;
        @Nullable
        private Set<Class<? extends RuntimeException>> transientExceptions;

        public HandlerPropertiesBuilder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public HandlerPropertiesBuilder retryDelayMs(int retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
            return this;
        }

        public HandlerPropertiesBuilder exponentialBackoff(boolean exponentialBackoff) {
            this.exponentialBackoff = exponentialBackoff;
            return this;
        }

        public HandlerPropertiesBuilder maxPoll(int maxPoll) {
            this.maxPoll = maxPoll;
            return this;
        }

        public HandlerPropertiesBuilder pollInterval(int pollInterval) {
            this.pollInterval = pollInterval;
            return this;
        }

        public HandlerPropertiesBuilder requeueInterval(int requeueInterval) {
            this.requeueInterval = requeueInterval;
            return this;
        }

        public HandlerPropertiesBuilder maxParallelTasks(int maxParallelTasks) {
            this.maxParallelTasks = maxParallelTasks;
            return this;
        }

        public HandlerPropertiesBuilder processingExpire(Duration processingExpire) {
            this.processingExpire = processingExpire;
            return this;
        }

        public HandlerPropertiesBuilder processingGracePeriod(Duration processingGracePeriod) {
            this.processingGracePeriod = processingGracePeriod;
            return this;
        }

        public HandlerPropertiesBuilder taskHandler(TaskHandler taskHandler) {
            this.taskHandler = taskHandler;
            return this;
        }

        public HandlerPropertiesBuilder taskHandlerType(Class<?> taskHandlerType) {
            this.taskHandlerType = taskHandlerType;
            return this;
        }

        public HandlerPropertiesBuilder autoStartEnabled(boolean autoStartEnabled) {
            this.autoStartEnabled = autoStartEnabled;
            return this;
        }

        public HandlerPropertiesBuilder transientExceptions(@Nullable Set<Class<? extends RuntimeException>> transientExceptions) {
            this.transientExceptions = transientExceptions == null ? Collections.emptySet() : Collections.unmodifiableSet(transientExceptions);
            return this;
        }

        @Override
        public HandlerProperties build() {
            taskHandlerType = discoverTaskHandlerType(taskHandler);
            checkRequiredFields();
            return new HandlerProperties(this);
        }

        @Nullable
        private Class<?> discoverTaskHandlerType(@Nullable TaskHandler taskHandler) {
            if (taskHandler == null) {
                return null;
            }

            try {
                return GenericTypeUtil.getGenericTypeFromInterface(taskHandler.getClass(), TaskHandler.class);
            } catch (RuntimeException ex) {
                Assert.notNull(taskHandlerType, "TaskHandlerType cannot be discover. It can be set manually using taskHandlerType when registering the task ");
                return taskHandlerType;
            }
        }
    }
}

