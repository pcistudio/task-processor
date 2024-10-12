package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.register.MysqlTaskStorageSetup;
import com.pcistudio.task.procesor.register.TaskStorageSetup;
import com.pcistudio.task.procesor.writer.MysqlTaskWriter;
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
    TaskWriter taskWriter(StorageResolver storageResolver) {///this in the builder
        return new MysqlTaskWriter(jdbcTemplate, storageResolver);
    }
}
