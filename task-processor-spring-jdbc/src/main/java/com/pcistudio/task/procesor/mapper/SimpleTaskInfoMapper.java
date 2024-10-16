package com.pcistudio.task.procesor.mapper;

import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskMetadata;
import com.pcistudio.task.procesor.util.decoder.MessageDecoding;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public class SimpleTaskInfoMapper implements RowMapper<TaskInfo> {
    private final String partitionId;

    @Override
    public TaskInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return TaskInfo.builder()
                .id(rs.getLong("id"))
//                .batchId(UUID.fromString(rs.getString("batch_id")))
                .partitionId(partitionId)
                .status(ProcessStatus.valueOf(rs.getString("status")))
                .version(rs.getLong("version"))
                .executionTime(rs.getTimestamp("execution_time").toInstant())
                .payloadBytes(rs.getBytes("payload"))
                .retryCount(rs.getInt("retry_count"))
                .handlerName(rs.getString("handler_name"))
                .build();
    }
}
