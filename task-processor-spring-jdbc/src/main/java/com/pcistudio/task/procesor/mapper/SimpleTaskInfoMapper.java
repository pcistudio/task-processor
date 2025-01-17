package com.pcistudio.task.procesor.mapper;

import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class SimpleTaskInfoMapper implements RowMapper<TaskInfo> {
    private final String partitionId;

    String getPartitionId() {
        return partitionId;
    }

    @Override
    public TaskInfo mapRow(ResultSet rs, int rowNum) throws SQLException {

        return TaskInfo.builder()
                .id(rs.getLong("id"))
//                .batchId(UUID.fromString(rs.getString("batch_id")))
                .partitionId(partitionId)
                .status(ProcessStatus.from(rs.getString("status")))
                .version(rs.getLong("version"))
                .executionTime(rs.getTimestamp("execution_time").toInstant())
                .payloadBytes(rs.getBytes("payload"))
                .retryCount(rs.getInt("retry_count"))
                .handlerName(rs.getString("handler_name"))
                .build();

    }
}
