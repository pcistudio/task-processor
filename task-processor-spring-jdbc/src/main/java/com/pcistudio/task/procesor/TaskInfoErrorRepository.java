package com.pcistudio.task.procesor;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@RequiredArgsConstructor
class TaskInfoErrorRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    @Transactional
    public void saveError(String tableName, TaskInfoError taskInfoError) {
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        insert into %s (id, task_id, error_message, created_at)
                        values (?, ?, ?)
                        """.formatted(tableName),
                taskInfoError.getId().toString(), taskInfoError.getTaskId(), taskInfoError.getErrorMessage(), now
        );
    }

}
