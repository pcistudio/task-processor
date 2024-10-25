package com.pcistudio.task.procesor.handler;

import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.util.Assert;
import com.pcistudio.task.procesor.util.GenericTypeUtil;
import com.pcistudio.task.procesor.util.decoder.MessageDecoding;
import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class TaskProcessingContext {
    private HandlerPropertiesWrapper handlerProperties;
    private TaskHandler taskHandler;
    private TaskInfoService taskInfoService;
    //TODO This should go in the properties
    // ignore getter generation lombok

    private Set<Class<? extends RuntimeException>> transientExceptions;
    private RetryManager retryManager;
    private MessageDecoding messageDecoding;

    private TaskProcessingContext() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Class getTaskHandlerType() {
        return GenericTypeUtil.getGenericTypeFromSuperclass(taskHandler.getClass());
    }


    public boolean isTransient(RuntimeException exception) {
        if (transientExceptions.contains(exception.getClass())) {
            return true;
        }

        for (Class<? extends RuntimeException> transientExceptionClass : transientExceptions) {
            if (transientExceptionClass.isInstance(exception)) {
                transientExceptions.add((Class<RuntimeException>)exception.getClass());
                return true;
            }
        }
        return false;
    }

    public static class Builder {
        private HandlerPropertiesWrapper handlerProperties;
        private TaskHandler taskHandler;
        private TaskInfoService taskInfoService;
        private RetryManager retryManager;
        private MessageDecoding messageDecoding;

        public Builder handlerProperties(HandlerPropertiesWrapper handlerProperties) {
            this.handlerProperties = handlerProperties;
            return this;
        }

        public Builder taskHandler(TaskHandler taskHandler) {
            this.taskHandler = taskHandler;
            return this;
        }

        public Builder taskInfoService(TaskInfoService taskInfoService) {
            this.taskInfoService = taskInfoService;
            return this;
        }

        public Builder retryManager(RetryManager retryManager) {
            this.retryManager = retryManager;
            return this;
        }

        public Builder messageDecoding(MessageDecoding messageDecoding) {
            this.messageDecoding = messageDecoding;
            return this;
        }

        public TaskProcessingContext build() {
            TaskProcessingContext context = new TaskProcessingContext();
            Assert.notNull(handlerProperties, "Handler properties cannot be null");
            Assert.notNull(handlerProperties.getTransientExceptions(), "Transient exceptions cannot be null");

            context.handlerProperties = handlerProperties;
            context.taskHandler = taskHandler;
            context.taskInfoService = taskInfoService;
            context.transientExceptions = new HashSet<>(handlerProperties.getTransientExceptions());
            context.retryManager = retryManager;
            context.messageDecoding = messageDecoding;
            return context;
        }
    }
}
