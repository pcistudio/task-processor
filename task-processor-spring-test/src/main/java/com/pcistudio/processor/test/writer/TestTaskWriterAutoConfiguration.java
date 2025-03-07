package com.pcistudio.processor.test.writer;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.writer.H2TaskInfoWriter;
import com.pcistudio.task.procesor.writer.TaskInfoWriter;
import com.pcistudio.task.processor.config.TaskWriterAutoConfiguration;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(before = TaskWriterAutoConfiguration.class)
public class TestTaskWriterAutoConfiguration {

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final JdbcTemplate jdbcTemplate;

    public TestTaskWriterAutoConfiguration(@Qualifier("taskProcessorJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    TaskInfoWriter taskInfoWriter(StorageResolver storageResolver) {
        return new H2TaskInfoWriter(jdbcTemplate, storageResolver);
    }
}