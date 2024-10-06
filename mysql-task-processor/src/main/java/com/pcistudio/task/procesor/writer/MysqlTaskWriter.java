package com.pcistudio.task.procesor.writer;

import com.pcistudio.task.procesor.ProcessStatus;
import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.TaskInfo;
import com.pcistudio.task.procesor.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class MysqlTaskWriter implements TaskWriter {

    private final JdbcTemplate jdbcTemplate;
    private final StorageResolver storageResolver;

    @Override
    public TaskInfo<Object> writeTasks(String handlerName, Object payload) {
        String tableName = storageResolver.resolveStorageName(handlerName);
        var taskInfo = TaskInfo.builder()
                .handlerName(handlerName)
                .payload(payload)
                .version(1L)
                .batchId(UUID.randomUUID())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .retryCount(0)
                .status(ProcessStatus.PENDING)
                .objectType(payload.getClass().getCanonicalName())
                .build();
        jdbcTemplate.update("""
                INSERT INTO %s (
                batch_id,
                status,
                last_error_message,
                version1,
                created_at,
                updated_at,
                payload,
                retry_count,
                handler_name,
                object_type
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(tableName),
                taskInfo.getBatchId(),
                taskInfo.getStatus().name(),
                taskInfo.getLastErrorMessage(),
                taskInfo.getVersion(),
                taskInfo.getCreatedAt(),
                taskInfo.getUpdatedAt(),
                JsonUtil.toJson(taskInfo.getPayload()),
                taskInfo.getRetryCount(),
                taskInfo.getHandlerName(),
                taskInfo.getObjectType());
        Long id = jdbcTemplate.queryForObject("SELECT last_insert_id()", Long.class);
        taskInfo.setId(id);

        return taskInfo;

    }

    @Override
    @Transactional
    public List<TaskInfo<Object>> writeTasks(String handlerName, Collection<Object> payload) {
        return payload.stream()
                .map(p -> writeTasks(handlerName, p))
                .toList();
    }
}
