package com.contact.manager.notification;

import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.config.TaskWriterConfiguration;
import com.pcistudio.task.procesor.register.ProcessorRegisterImpl;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskProcessorConfig extends TaskWriterConfiguration {

    @Override
    protected void addTask(ProcessorRegisterImpl.Builder builder) {
        builder.register(
                HandlerProperties.builder()
                        .handlerName("email")
                        .tableName("email")
                        .build()
        );
    }
}
