CREATE TABLE IF NOT EXISTS task_table (
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
                );

CREATE TABLE IF NOT EXISTS task_table_error (
                   id CHAR(36) PRIMARY KEY,
                   task_id BIGINT NOT NULL,
                   partition_id VARCHAR(36),
                   error_message VARCHAR(1024),
                   created_at TIMESTAMP NOT NULL,
                   FOREIGN KEY (task_id) REFERENCES task_table(id)
                           ON DELETE CASCADE
                );