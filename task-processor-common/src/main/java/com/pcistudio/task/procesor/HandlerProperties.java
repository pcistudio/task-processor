package com.pcistudio.task.procesor;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Getter
@SuperBuilder
public class HandlerProperties extends HandlerWriteProperties {
    private String handlerClass;
    private String handlerMethod;
    @Builder.Default
    private int maxRetries = 3;
    @Builder.Default
    private int retryDelayMs = 1000;
    private int maxParallelTasks;
    @Builder.Default
    private boolean exponentialBackoff = false;
    @Builder.Default
    private int maxPoll = 16;
    @Builder.Default
    private Set<String> transientExceptions = new HashSet<>();
    private String payloadType;
    @Builder.Default
    private Duration processingExpire = Duration.ofMinutes(5);
}
