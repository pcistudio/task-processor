package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.handler.TaskHandler;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Getter
@SuperBuilder
public class HandlerProperties extends HandlerWriteProperties {
//    private String handlerClass;
//    private String handlerMethod;
    @Builder.Default
    private int maxRetries = 3;
    @Builder.Default
    private int retryDelayMs = 1000;
    @Builder.Default
    private boolean exponentialBackoff = false;
    /**
     * Maximum number of tasks to poll from the queue
     */
    @Builder.Default
    private int maxPoll = 16;
    @Builder.Default
    private int pollInterval = 60000;
    @Builder.Default
    private int requeueInterval = 300000;
    @Builder.Default
    private int taskExecutionTimeout = 180000;
    @Builder.Default
    private int maxParallelTasks = 1;
    @Builder.Default
    private Set<Class<? extends RuntimeException>> transientExceptions = new HashSet<>();
    @Builder.Default
    private Duration processingExpire = Duration.ofMinutes(5);
    @Builder.Default
    private Duration processingGracePeriod = Duration.ofMinutes(0);
    private TaskHandler taskHandler;
    private Class<?> taskHandlerType;

}
