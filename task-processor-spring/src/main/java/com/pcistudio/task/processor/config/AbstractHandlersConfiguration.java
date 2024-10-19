package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.register.*;
import com.pcistudio.task.procesor.util.encoder.JsonMessageEncoding;
import com.pcistudio.task.procesor.util.encoder.MessageEncoding;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;


@RequiredArgsConstructor
public abstract class AbstractHandlersConfiguration {

    @Bean
    @ConditionalOnMissingBean(HandlerManager.class)
    HandlerManagerImpl processorRegister(TaskStorageSetup taskStorageSetup) {
        HandlerManagerImpl.Builder builder = new HandlerManagerImpl.Builder();
        builder.taskTableSetup(taskStorageSetup);
        addTask(builder);
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(StorageResolver.class)
    StorageResolver storageResolver(HandlerManagerImpl handlerManager) {

        Assert.notNull(handlerManager, "handlerManager is required");
        return new DefaultStorageResolver(handlerManager);
    }

    @Bean("jsonMessageEncoding")
    @ConditionalOnMissingBean(MessageEncoding.class)
    MessageEncoding messageEncoding() {
        return new JsonMessageEncoding();
    }

    protected abstract void addTask(HandlerManagerImpl.Builder builder);
}
