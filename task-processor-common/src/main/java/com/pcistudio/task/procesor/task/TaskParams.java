package com.pcistudio.task.procesor.task;


import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;


@Getter
public final class TaskParams {
    private final String handlerName;
    private final Object payload;
    @Nullable
    private final Instant executionTime;
    @Nullable
    private final Duration delay;

    private TaskParams(TaskParamsBuilder builder) {
        this.handlerName = Objects.requireNonNull(builder.handlerName, "handlerName must not be null");
        this.payload = Objects.requireNonNull(builder.payload, "payload must not be null");
        this.executionTime = builder.executionTime;
        this.delay = builder.delay;
    }

    public static TaskParamsBuilder builder() {
        return new TaskParamsBuilder();
    }

    public static final class TaskParamsBuilder {
        @Nullable
        private String handlerName;
        @Nullable
        private Object payload;
        @Nullable
        private Instant executionTime;
        @Nullable
        private Duration delay;

        private TaskParamsBuilder() {
        }

        public TaskParamsBuilder handlerName(String handlerName) {
            this.handlerName = handlerName;
            return this;
        }

        public TaskParamsBuilder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public TaskParamsBuilder executionTime(Instant executionTime) {
            this.executionTime = executionTime;
            return this;
        }

        public TaskParamsBuilder delay(Duration delay) {
            this.delay = delay;
            return this;
        }

        public TaskParams build() {
            return new TaskParams(this);
        }
    }

}
