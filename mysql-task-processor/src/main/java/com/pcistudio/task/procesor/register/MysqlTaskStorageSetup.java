package com.pcistudio.task.procesor.register;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
public class MysqlTaskStorageSetup implements TaskStorageSetup {
    private final JdbcTemplate jdbcTemplate;

    public MysqlTaskStorageSetup(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createStorage(String tableName) {

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS %s (
                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                   batch_id VARCHAR(50) NOT NULL,
                   status VARCHAR(50) NOT NULL,
                   last_error_message VARCHAR(255),
                   version1 BIGINT NOT NULL,
                   created_at TIMESTAMP NOT NULL,
                   updated_at TIMESTAMP NOT NULL,
                   payload TEXT NOT NULL,
                   retry_count INT NOT NULL,
                   handler_name VARCHAR(70) NOT NULL,
                   object_type VARCHAR(128) NOT NULL
                );""".formatted(tableName));

        log.info("Table created successfully for table {}", tableName);
    }
}
