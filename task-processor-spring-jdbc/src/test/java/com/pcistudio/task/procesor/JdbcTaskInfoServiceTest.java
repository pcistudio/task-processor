package com.pcistudio.task.procesor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ContextConfiguration(classes = JdbcConfig.class)
@Sql(value = {"/task-schema.sql", "/task-data.sql"}
)
@Slf4j
class JdbcTaskInfoServiceTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void test() {
        long start = System.currentTimeMillis();
        JdbcTaskInfoService jdbcTaskInfoService = new JdbcTaskInfoService(StorageResolver.IDENTITY, "partitionId", jdbcTemplate, Clock.systemUTC());

        List<TaskInfo<Object>> taskTable = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(10, taskTable.size());
        taskTable.forEach(taskInfo -> jdbcTaskInfoService.markTaskCompleted(taskInfo));
        log.info("Time to process 10 tasks: {}", System.currentTimeMillis() - start);


        List<TaskInfo<Object>> taskTable2 = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(2, taskTable2.size());
        log.debug("--------------------------------------------------------------------");
        taskTable2.forEach(taskInfo -> jdbcTaskInfoService.markTaskCompleted(taskInfo));
        log.info("Time to process 2 tasks: {}", System.currentTimeMillis() - start);

        List<TaskInfo<Object>> taskTable3 = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(0, taskTable3.size());

        log.info("time until this point: {}", System.currentTimeMillis() - start);
        long remain = 8100 - (System.currentTimeMillis() - start);
        if (remain > 0) {
            try {
                Thread.sleep(remain);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<TaskInfo<Object>> taskTable4 = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(5, taskTable4.size());
    }

//    @Test
//    void test2() {
//        long start = System.currentTimeMillis();
//        JdbcTaskInfoService jdbcTaskInfoService = new JdbcTaskInfoService(StorageResolver.IDENTITY, "partitionId", jdbcTemplate, Clock.systemUTC());
//
//        List<TaskInfo<Object>> taskTable = jdbcTaskInfoService.poll("task_table", 10);
//        assertEquals(10, taskTable.size());
//        taskTable.forEach(taskInfo -> jdbcTaskInfoService.markTaskCompleted(taskInfo));
//        log.info("Time to process 10 tasks: {}", System.currentTimeMillis() - start);
//
//
//        List<TaskInfo<Object>> taskTable2 = jdbcTaskInfoService.poll("task_table", 10);
//        assertEquals(2, taskTable2.size());
//        log.debug("--------------------------------------------------------------------");
//        taskTable.forEach(taskInfo -> jdbcTaskInfoService.markTaskCompleted(taskInfo));
//        log.info("Time to process 2 tasks: {}", System.currentTimeMillis() - start);
//
//        List<TaskInfo<Object>> taskTable3 = jdbcTaskInfoService.poll("task_table", 10);
//        assertEquals(0, taskTable3.size());
//
//        log.info("time until this point: {}", System.currentTimeMillis() - start);
//        long remain = 8100 - (System.currentTimeMillis() - start);
//        if (remain > 0) {
//            try {
//                Thread.sleep(remain);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        List<TaskInfo<Object>> taskTable4 = jdbcTaskInfoService.poll("task_table", 10);
//        assertEquals(5, taskTable4.size());
//    }
}