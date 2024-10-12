package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.JdbcTaskInfoService;
import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.handler.TaskInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;

@ConditionalOnClass(name = "com.pcistudio.task.procesor.JdbcTaskInfoService")
@ConditionalOnBean(value = JdbcTemplate.class)
@Configuration
public class JdbcTaskInfoServiceAutoConfiguration {

    @Value("${task.processor.partitionId:#{T(java.util.UUID).randomUUID().toString()}}")
    private String partitionId;

    @Bean
    TaskInfoService taskInfoService(StorageResolver storageResolver, JdbcTemplate jdbcTemplate, Clock clock) {
        return new JdbcTaskInfoService(storageResolver, partitionId, jdbcTemplate, clock);
    }

    @ConditionalOnMissingBean(value = Clock.class)
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
