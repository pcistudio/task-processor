package com.pcistudio.processor.test.config;

import com.pcistudio.task.processor.config.TaskProcessorJdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@AutoConfiguration(before = TaskProcessorJdbcTemplateAutoConfiguration.class)
public class TestTaskProcessorJdbcTemplateAutoConfiguration {

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