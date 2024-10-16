package com.pcistudio.task.procesor.mapper;

import com.pcistudio.task.procesor.task.TaskInfoError;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TaskInfoErrorMapper implements RowMapper<TaskInfoError> {
    @Override
    public TaskInfoError mapRow(ResultSet rs, int rowNum) throws SQLException {
        return TaskInfoError.builder()
                .id(UUID.fromString(rs.getString("id")))
                .taskId(rs.getLong("task_id"))
                .partitionId(rs.getString("partition_id"))
                .errorMessage(rs.getString("error_message"))
                .build();
    }
}
