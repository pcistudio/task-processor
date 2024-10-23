package com.pcistudio.task.procesor.writer;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class MariadbTaskInfoWriter implements TaskInfoWriter {
    private final JdbcTemplate jdbcTemplate;
    private final StorageResolver storageResolver;

    @Override
    public TaskMetadata writeTasks(TaskInfo taskInfo) {
        String tableName = storageResolver.resolveStorageName(taskInfo.getHandlerName());

        jdbcTemplate.update("""
                INSERT INTO %s (
                batch_id,
                status,
                version,
                created_at,
                updated_at,
                execution_time,
                payload,
                retry_count,
                handler_name,
                object_type
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(tableName),
                taskInfo.getBatchId(),
                taskInfo.getStatus().name(),
                taskInfo.getVersion(),
                taskInfo.getCreatedAt(),
                taskInfo.getUpdatedAt(),
                taskInfo.getExecutionTime(),
                taskInfo.getPayloadBytes(),
                taskInfo.getRetryCount(),
                taskInfo.getHandlerName(),
                taskInfo.getObjectType());
        Long id = jdbcTemplate.queryForObject("SELECT last_insert_id()", Long.class);
        taskInfo.setId(id);

        return taskInfo;

    }

    @Override
    @Transactional
    public List<TaskMetadata> writeTasks(Collection<TaskInfo> taskInfoList) {
        return taskInfoList.stream()
                .map(this::writeTasks)
                .toList();
    }
}
