package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
public class TaskProcessingContext {
    private HandlerPropertiesWrapper handlerProperties;
    private TaskHandler taskHandler;
    private TaskInfoService taskInfoService;
    //TODO This should go in the properties
    private Set<RuntimeException> transientExceptions;
    private RetryManager retryManager;

}
