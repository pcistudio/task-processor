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

import java.time.Clock;

@Slf4j
public abstract class AbstractHandlersConfiguration {

    @Bean
    @ConditionalOnMissingBean(HandlerManager.class)
    HandlerManagerImpl processorRegister(TaskStorageSetup taskStorageSetup) {
        HandlerManagerImpl.Builder builder = HandlerManagerImpl.builder()
                .taskTableSetup(taskStorageSetup);
        configureHandler(builder);
        return builder.build();
    }

    // TODO: For next version this need to be implemented thinking in only one task at the time
    // Meaning for each task you will create a different bean
    protected abstract void configureHandler(HandlerManagerImpl.Builder builder);

    //TODO Add Actuator endpoint
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


    @ConditionalOnMissingBean(Clock.class)
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
