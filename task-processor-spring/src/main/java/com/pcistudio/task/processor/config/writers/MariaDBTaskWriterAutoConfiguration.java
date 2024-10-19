package com.pcistudio.task.processor.config.writers;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.writer.MysqlTaskInfoWriter;
import com.pcistudio.task.procesor.writer.TaskInfoWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
@Configuration
@ConditionalOnClass(name={"org.springframework.jdbc.core.JdbcTemplate", "org.mariadb.jdbc.Driver"  })
@ConditionalOnProperty(prefix = "spring.task.processor.writer", name = "enabled", havingValue = "true")
public class MariaDBTaskWriterAutoConfiguration {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    TaskInfoWriter taskInfoWriter(StorageResolver storageResolver) {///this in the builder
        return new MysqlTaskInfoWriter(jdbcTemplate, storageResolver);
    }
}
