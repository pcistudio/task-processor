package com.pcistudio.processor.test.config;

import com.pcistudio.task.procesor.register.H2TaskStorageSetup;
import com.pcistudio.task.procesor.register.TaskStorageSetup;
import com.pcistudio.task.procesor.template.LoggingJdbcTemplate;
import com.pcistudio.task.processor.config.TaskProcessorJdbcTemplateAutoConfiguration;
import com.pcistudio.task.processor.util.TaskProcessorDataSourceHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@AutoConfiguration(before = TaskProcessorJdbcTemplateAutoConfiguration.class)
public class TestTaskProcessorJdbcTemplateAutoConfiguration {

    @Configuration
    @ConditionalOnProperty(prefix = "spring.task.logging", name = "template", havingValue = "true", matchIfMissing = true)
    static class DefaultJdbcTemplate {
        @Bean
        @ConditionalOnMissingBean(name = "taskProcessorJdbcTemplate")
        JdbcTemplate taskProcessorJdbcTemplate() {
            EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("testdb;MODE=MYSQL;DATABASE_TO_LOWER=TRUE")
                    .build();
            return new JdbcTemplate(db);
        }
    }

    @ConditionalOnProperty(prefix = "spring.task.logging", name = "template", havingValue = "true")
    static class LoggingTemplate {
        @Bean
        @ConditionalOnMissingBean(name = "taskProcessorJdbcTemplate")
        JdbcTemplate taskProcessorJdbcTemplate() {
            EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("testdb;MODE=MYSQL;DATABASE_TO_LOWER=TRUE")
                    .build();
            return new LoggingJdbcTemplate(db);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    TaskStorageSetup taskStorageSetup(@Qualifier("taskProcessorJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new H2TaskStorageSetup(jdbcTemplate);
    }
}