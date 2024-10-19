package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.util.encoder.MessageEncoding;
import com.pcistudio.task.procesor.writer.TaskInfoWriter;
import com.pcistudio.task.procesor.writer.TaskWriter;
import com.pcistudio.task.processor.config.writers.H2TaskWriterAutoConfiguration;
import com.pcistudio.task.processor.config.writers.MariaDBTaskWriterAutoConfiguration;
import com.pcistudio.task.processor.config.writers.MySqlTaskWriterAutoConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(prefix = "spring.task.processor.writer", name = "enabled", havingValue = "true")
@Import({
        TaskStorageSetupConfiguration.class,
        H2TaskWriterAutoConfiguration.class,
        MariaDBTaskWriterAutoConfiguration.class,
        MySqlTaskWriterAutoConfiguration.class,
})
public class TaskWriterAutoConfiguration {
    @Bean
    TaskWriter taskWriter(TaskInfoWriter taskInfoWriter, MessageEncoding messageEncoding) {///this in the builder
        return new TaskWriter(taskInfoWriter, messageEncoding);
    }
}
