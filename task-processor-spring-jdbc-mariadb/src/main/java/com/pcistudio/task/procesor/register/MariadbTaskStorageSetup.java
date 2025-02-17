package com.pcistudio.task.procesor.register;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class MariadbTaskStorageSetup implements TaskStorageSetup {
    private final JdbcTemplate jdbcTemplate;

    public MariadbTaskStorageSetup(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
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
                   payload BLOB NOT NULL,
                   retry_count INT NOT NULL,
                   partition_id VARCHAR(36),
                   read_token VARCHAR(36),
                   handler_name VARCHAR(70) NOT NULL,
                   object_type VARCHAR(128) NOT NULL
                );""".formatted(tableName));
//        index in status and in execution_time

        jdbcTemplate.execute("""
                CREATE Index IF NOT EXISTS %s_execution_time_idx ON %s (execution_time);
                """.formatted(tableName, tableName));

        jdbcTemplate.execute("""
                create index IF NOT EXISTS %s_execution_time_status_handler_idx on %s(handler_name,status,execution_time);
                """.formatted(tableName, tableName));

        jdbcTemplate.execute("""
                CREATE Index IF NOT EXISTS %s_updated_at_status_idx ON %s (status,updated_at);
                """.formatted(tableName, tableName));

        jdbcTemplate.execute("""
                CREATE Index IF NOT EXISTS %s_read_token_idx ON %s (read_token);
                """.formatted(tableName, tableName));

        log.info("Table created successfully for table {}", tableName);
        String errorTableName = "%s_error".formatted(tableName);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS %s (
                   id CHAR(36) PRIMARY KEY,
                   task_id BIGINT NOT NULL,
                   partition_id VARCHAR(36),
                   error_message VARCHAR(512),
                   created_at TIMESTAMP NOT NULL,
                   FOREIGN KEY (task_id) REFERENCES %s(id)
                           ON DELETE CASCADE
                );""".formatted(errorTableName, tableName));

        jdbcTemplate.execute("""
                CREATE Index IF NOT EXISTS %s_created_at_idx ON %s (created_at);
                """.formatted(errorTableName, errorTableName));

        log.info("Table created successfully for table {}", errorTableName);
    }
}
