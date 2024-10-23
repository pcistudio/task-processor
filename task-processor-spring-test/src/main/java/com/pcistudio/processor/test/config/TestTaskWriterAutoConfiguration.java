package com.pcistudio.processor.test.config;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.register.H2TaskStorageSetup;
import com.pcistudio.task.procesor.register.TaskStorageSetup;
import com.pcistudio.task.procesor.writer.H2TaskInfoWriter;
import com.pcistudio.task.procesor.writer.TaskInfoWriter;
import com.pcistudio.task.processor.config.TaskWriterAutoConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(before = TaskWriterAutoConfiguration.class)
public class TestTaskWriterAutoConfiguration {

    private final JdbcTemplate jdbcTemplate;

    public TestTaskWriterAutoConfiguration(@Qualifier("taskProcessorJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    TaskInfoWriter taskInfoWriter(StorageResolver storageResolver) {
        return new H2TaskInfoWriter(jdbcTemplate, storageResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    TaskStorageSetup taskStorageSetup() {
        return new H2TaskStorageSetup(jdbcTemplate);
    }
}