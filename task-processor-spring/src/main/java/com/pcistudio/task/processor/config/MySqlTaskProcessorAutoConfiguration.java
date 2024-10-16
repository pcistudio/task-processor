package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.register.MysqlTaskStorageSetup;
import com.pcistudio.task.procesor.register.TaskStorageSetup;
import com.pcistudio.task.procesor.util.encoder.MessageEncoding;
import com.pcistudio.task.procesor.writer.MysqlTaskInfoWriter;
import com.pcistudio.task.procesor.writer.TaskInfoWriter;
import com.pcistudio.task.procesor.writer.TaskWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
@Configuration
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnProperty(prefix = "spring.task.processor.writer", name = "enabled", havingValue = "true")
public class MySqlTaskProcessorAutoConfiguration {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    TaskStorageSetup taskStorageSetup() {
        return new MysqlTaskStorageSetup(jdbcTemplate);
    }

    @Bean
    TaskInfoWriter taskInfoWriter(StorageResolver storageResolver, MessageEncoding messageEncoding) {///this in the builder
        return new MysqlTaskInfoWriter(jdbcTemplate, storageResolver);
    }

    @Bean
    TaskWriter taskWriter(TaskInfoWriter taskInfoWriter, MessageEncoding messageEncoding) {///this in the builder
        return new TaskWriter(taskInfoWriter, messageEncoding);
    }
}
