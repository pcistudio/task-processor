package com.contact.manager.notification;

import com.pcistudio.task.procesor.HandlerProperties;

import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;

import java.time.Duration;
import java.util.Set;

@Configuration
public class TaskProcessorConfig extends AbstractHandlersConfiguration {
    @Autowired
    private NotificationTaskHandler notificationTaskHandler;

    @Override
    protected void addTask(HandlerManagerImpl.Builder builder) {
        builder.register(
                HandlerProperties.builder()
                        .handlerName("email")
                        .tableName("email")
                        .requeueInterval(120000)
                        .processingExpire(Duration.ofMinutes(2))
                        .transientExceptions(Set.of(TransientDataAccessException.class))
                        .taskHandler(notificationTaskHandler)
                        .build()
        );
    }
}
