package com.contact.manager.notification;

import com.pcistudio.task.procesor.HandlerProperties;

import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;

import java.util.Set;

@Configuration
public class TaskProcessorConfig extends AbstractHandlersConfiguration {

    @Override
    protected void addTask(HandlerManagerImpl.Builder builder) {
        builder.register(
                HandlerProperties.builder()
                        .handlerName("email")
                        .tableName("email")
                        .transientExceptions(Set.of(TransientDataAccessException.class))
                        .build()
        );
    }
}
