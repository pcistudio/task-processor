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
//TODO object_type probably not needed
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS %s (
                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                   batch_id VARCHAR(50) NOT NULL,
                   status VARCHAR(50) NOT NULL,
                   version BIGINT NOT NULL,
                   created_at TIMESTAMP NOT NULL,
                   updated_at TIMESTAMP NOT NULL,
                   execution_time TIMESTAMP NOT NULL,
                   payload TEXT NOT NULL,
                   retry_count INT NOT NULL,
                   partition VARCHAR(36),
                   handler_name VARCHAR(70) NOT NULL,
                   object_type VARCHAR(128) NOT NULL
                );""".formatted(tableName));
//        index in status and in execution_time

        log.info("Table created successfully for table {}", tableName);
        String errorTableName = "%s_error".formatted(tableName);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS %s (
                   id CHAR(36) PRIMARY KEY,
                   task_id BIGINT NOT NULL,
                   partition VARCHAR(36),
                   last_error_message VARCHAR(255),
                   created_at TIMESTAMP NOT NULL,
                   FOREIGN KEY (task_id) REFERENCES %s(id)
                           ON DELETE CASCADE
                );""".formatted(errorTableName, tableName));

        log.info("Table created successfully for table {}", errorTableName);
    }
}
