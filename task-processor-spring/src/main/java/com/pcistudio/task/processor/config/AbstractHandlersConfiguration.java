package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.register.DefaultStorageResolver;
import com.pcistudio.task.procesor.register.HandlerManager;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.procesor.register.TaskStorageSetup;
import com.pcistudio.task.procesor.util.encoder.JsonMessageEncoding;
import com.pcistudio.task.procesor.util.encoder.MessageEncoding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

@Slf4j
public abstract class AbstractHandlersConfiguration {

    @Bean
    @ConditionalOnMissingBean(HandlerManager.class)
    HandlerManagerImpl processorRegister(TaskStorageSetup taskStorageSetup) {
        HandlerManagerImpl.Builder builder = new HandlerManagerImpl.Builder();
        builder.taskTableSetup(taskStorageSetup);
        addTask(builder);
        return builder.build();
    }

    // TODO: For next version this need to be implemented thinking in only one task at the time
    // Meaning for each task you will create a different bean
    protected abstract void addTask(HandlerManagerImpl.Builder builder);


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


}
