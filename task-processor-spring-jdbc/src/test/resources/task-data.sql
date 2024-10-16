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
                   partition VARCHAR(36),
                   handler_name VARCHAR(70) NOT NULL,
                   object_type VARCHAR(128) NOT NULL
                );

INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('batch1', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload1', 0, 'task_table', 'object1');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('batch2', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload2', 0, 'task_table', 'object2');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('batch3', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload3', 0, 'task_table', 'object3');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('batch4', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload4', 0, 'task_table', 'object4');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('batch5', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload5', 0, 'task_table', 'object5');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('batch6', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload6', 0, 'task_table', 'object6');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch7', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload7', 0, 'task_table', 'object7');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch8', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload8', 0, 'task_table', 'object8');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch9', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload9', 0, 'task_table', 'object9');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch10', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload10', 0, 'task_table', 'object10');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch11', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload11', 0, 'task_table', 'object11');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch12', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload12', 0, 'task_table', 'object12');
-- 2 with status = 'FAILED'
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch13', 'FAILED', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload13', 0, 'task_table', 'object13');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch14', 'FAILED', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload14', 0, 'task_table', 'object14');
-- 2 with status = 'COMPLETED'
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch15', 'COMPLETED', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload15', 0, 'task_table', 'object15');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch16', 'COMPLETED', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload16', 0, 'task_table', 'object16');

-- 5 with status = 'PENDING' and execution_time in the future by 8 seconds from now.
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch17', 'PENDING', 1, now(), now(), DATEADD(SECOND, 8, NOW()), 'payload17', 0, 'task_table', 'object17');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch18', 'PENDING', 1, now(), now(), DATEADD(SECOND, 8, NOW()), 'payload18', 0, 'task_table', 'object18');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch19', 'PENDING', 1, now(), now(), DATEADD(SECOND, 8, NOW()), 'payload19', 0, 'task_table', 'object19');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch20', 'PENDING', 1, now(), now(), DATEADD(SECOND, 8, NOW()), 'payload20', 0, 'task_table', 'object20');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('batch21', 'PENDING', 1, now(), now(), DATEADD(SECOND, 8, NOW()), 'payload21', 0, 'task_table', 'object21');

