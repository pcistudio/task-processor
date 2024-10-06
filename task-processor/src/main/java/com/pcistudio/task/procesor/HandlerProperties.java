package com.pcistudio.task.procesor;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class HandlerProperties extends HandlerWriteProperties {
    private String handlerClass;
    private String handlerMethod;
    @Builder.Default
    private int maxRetryCount = 3;
    @Builder.Default
    private int retryIntervalMs = 1000;
    private int maxParallelTasks;
    @Builder.Default
    private boolean exponentialBackoff = false;
}
