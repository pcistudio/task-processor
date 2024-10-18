package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.mapper.TaskInfoErrorMapper;
import com.pcistudio.task.procesor.task.TaskInfoError;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
class TaskInfoErrorRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;
    private final TaskInfoErrorMapper taskInfoErrorMapper = new TaskInfoErrorMapper();

    @Transactional
    public void saveError(String tableName, TaskInfoError taskInfoError) {
        Instant now = Instant.now(clock);
        jdbcTemplate.update(
                """
                        insert into %s (id, task_id, error_message, created_at,partition_id)
                        values (?, ?, ?, ?, ?)
                        """.formatted(tableName),
                taskInfoError.getId(),
                taskInfoError.getTaskId(),
                taskInfoError.getErrorMessage().substring(0, Math.min(taskInfoError.getErrorMessage().length(), 512)),
                now,
                taskInfoError.getPartitionId()
        );
    }

    public List<TaskInfoError> getTaskErrors(String tableName, Long taskId) {
        return jdbcTemplate.query(
                """
                        select id, task_id, error_message, created_at, partition_id
                        from %s
                        where task_id = ?
                        order by created_at desc
                        """.formatted(tableName),
                taskInfoErrorMapper,
                taskId
        );
    }

    public void saveErrors(String tableName, List<TaskInfoError> timeoutTaskInfoError) {
        Instant now = Instant.now(clock);
        jdbcTemplate.batchUpdate(
                """
                        insert into %s (id, task_id, error_message, created_at, partition_id)
                        values (?, ?, ?, ?, ?)
                        """.formatted(tableName),
                timeoutTaskInfoError,
                timeoutTaskInfoError.size(),
                (ps, taskInfoError) -> {
                    ps.setObject(1, taskInfoError.getId());
                    ps.setLong(2, taskInfoError.getTaskId());
                    ps.setString(3, taskInfoError.getErrorMessage().substring(0, Math.min(taskInfoError.getErrorMessage().length(), 512)));
                    ps.setObject(4, now);
                    ps.setString(5, taskInfoError.getPartitionId());
                }
        );
    }
}
