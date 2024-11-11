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
-- 12 PENDING
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload1', 0, 'task_table', 'object1');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload2', 0, 'task_table', 'object2');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('550e8400-e29b-41d4-a716-446655440002', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload3', 0, 'task_table', 'object3');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('550e8400-e29b-41d4-a716-446655440003', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload4', 0, 'task_table', 'object4');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('550e8400-e29b-41d4-a716-446655440004', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload5', 0, 'task_table', 'object5');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
VALUES ('550e8400-e29b-41d4-a716-446655440005', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload6', 0, 'task_table', 'object6');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440007', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload7', 0, 'task_table', 'object7');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440008', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload8', 0, 'task_table', 'object8');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440009', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload9', 0, 'task_table', 'object9');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440010', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload10', 0, 'task_table', 'object10');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440011', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload11', 0, 'task_table', 'object11');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440012', 'PENDING', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload12', 0, 'task_table', 'object12');
-- 2 with status = 'FAILED'
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440013', 'FAILED', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload13', 0, 'task_table', 'object13');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440014', 'FAILED', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload14', 0, 'task_table', 'object14');
-- 2 with status = 'COMPLETED'
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440015', 'COMPLETED', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload15', 0, 'task_table', 'object15');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440016', 'COMPLETED', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'payload16', 0, 'task_table', 'object16');

-- 5 with status = 'PENDING' and execution_time in the future by 8 seconds from now.
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440017', 'PENDING', 1, now(), now(), DATEADD(SECOND, 8, NOW()), 'payload17', 0, 'task_table', 'object17');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440018', 'PENDING', 1, now(), now(), DATEADD(SECOND, 8, NOW()), 'payload18', 0, 'task_table', 'object18');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440019', 'PENDING', 1, now(), now(), DATEADD(SECOND, 8, NOW()), 'payload19', 0, 'task_table', 'object19');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440020', 'PENDING', 1, now(), now(), DATEADD(SECOND, 8, NOW()), 'payload20', 0, 'task_table', 'object20');
INSERT INTO task_table (batch_id, status, version, created_at, updated_at, execution_time, payload, retry_count, handler_name, object_type)
values ('550e8400-e29b-41d4-a716-446655440021', 'PENDING', 1, now(), now(), DATEADD(SECOND, 8, NOW()), 'payload21', 0, 'task_table', 'object21');

