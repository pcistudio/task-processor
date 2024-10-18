package com.contact.manager.notification;

import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.processor.config.TaskWriterConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskProcessorConfig extends TaskWriterConfiguration {

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
