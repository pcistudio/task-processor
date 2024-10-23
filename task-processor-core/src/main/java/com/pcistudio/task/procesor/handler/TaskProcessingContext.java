package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.util.GenericTypeUtil;
import com.pcistudio.task.procesor.util.decoder.MessageDecoding;
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
//    private Set<RuntimeException> transientExceptions;
    private RetryManager retryManager;
    private MessageDecoding messageDecoding;

    public Class getTaskHandlerType() {
        return GenericTypeUtil.getGenericTypeFromSuperclass(taskHandler.getClass());
    }


}
