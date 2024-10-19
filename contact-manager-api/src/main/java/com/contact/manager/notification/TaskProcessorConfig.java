package com.contact.manager.notification;

import com.pcistudio.task.procesor.HandlerProperties;

import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskProcessorConfig extends AbstractHandlersConfiguration {

    @Override
    protected void addTask(HandlerManagerImpl.Builder builder) {
        builder.register(
                HandlerProperties.builder()
                        .handlerName("email")
                        .tableName("email")
                        .build()
        );
    }
}
