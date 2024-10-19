package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.register.MysqlTaskStorageSetup;
import com.pcistudio.task.procesor.register.TaskStorageSetup;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
//@Configuration
//@ConditionalOnClass(JdbcTemplate.class)
public class TaskStorageSetupConfiguration {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    TaskStorageSetup taskStorageSetup() {
        return new MysqlTaskStorageSetup(jdbcTemplate);
    }

}
