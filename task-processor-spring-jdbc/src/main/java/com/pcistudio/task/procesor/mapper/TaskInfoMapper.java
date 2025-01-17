package com.pcistudio.task.procesor.mapper;

import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TaskInfoMapper implements RowMapper<TaskInfo> {
    @Override
    public TaskInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return TaskInfo.builder()
                .id(rs.getLong("id"))
                .batchId(UUID.fromString(rs.getString("batch_id")))
                .status(ProcessStatus.from(rs.getString("status")))
                .version(rs.getLong("version"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .updatedAt(rs.getTimestamp("updated_at").toInstant())
                .executionTime(rs.getTimestamp("execution_time").toInstant())
                .payloadBytes(rs.getBytes("payload"))
                .retryCount(rs.getInt("retry_count"))
                .handlerName(rs.getString("handler_name"))
                .objectType(rs.getString("object_type"))
                .build();
    }
}
