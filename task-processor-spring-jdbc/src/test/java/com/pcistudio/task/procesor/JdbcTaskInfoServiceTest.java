package com.pcistudio.task.procesor;

import com.pcistudio.task.procesor.handler.TaskHandler;
import com.pcistudio.task.procesor.page.Pageable;
import com.pcistudio.task.procesor.page.Sort;
import com.pcistudio.task.procesor.register.HandlerLookup;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskInfoError;
import com.pcistudio.task.procesor.util.MutableFixedClock;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@JdbcTest
@ContextConfiguration(classes = JdbcConfig.class)
@Sql(value = {"/task-schema.sql", "/task-data.sql"})
@Slf4j
class JdbcTaskInfoServiceTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HandlerLookup handlerLookup = new HandlerLookup() {
        public HandlerPropertiesWrapper getProperties(String handlerName) {
            TaskHandler<TaskInfo> taskInfoTaskHandler = (taskInfo) -> {
                log.info("taskInfo={}", taskInfo);
            };
            return new HandlerPropertiesWrapper(HandlerProperties.builder()
                    .handlerName(handlerName)
                    .tableName("task_table")
                    .processingExpire(Duration.ofSeconds(3))
                    .taskHandler(taskInfoTaskHandler)
                    .taskHandlerType(TaskInfo.class)
                    .build()
            );
        }

        public java.util.Iterator<HandlerPropertiesWrapper> getIterator() {
            return null;
        }
    };
    @Test
    @DisplayName("""
            Polling 10 tasks then try to poll 10 more tasks
            but only 2 tasks are available then wait for 8 seconds
            and poll 5 tasks that will be ready""")
    void test() {
        MutableFixedClock clock = new MutableFixedClock();

        JdbcTaskInfoService jdbcTaskInfoService = new JdbcTaskInfoService(StorageResolver.IDENTITY, "partitionId", jdbcTemplate, clock, handlerLookup);

        List<TaskInfo> taskTable = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(10, taskTable.size());
        taskTable.forEach(taskInfo -> jdbcTaskInfoService.markTaskCompleted(taskInfo));

        List<TaskInfo> taskTable2 = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(2, taskTable2.size());

        taskTable2.forEach(taskInfo -> jdbcTaskInfoService.markTaskCompleted(taskInfo));

        List<TaskInfo> taskTable3 = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(0, taskTable3.size());

        clock.increaseTime(Duration.ofSeconds(8));
        List<TaskInfo> taskTable4 = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(5, taskTable4.size());
    }

    @Test
    @DisplayName("Polling 10 task markTaskToRetry in 60 seconds then poll 2 more tasks and call markTaskCompleted " +
            "then there is not more task, so wait 60 seconds and it will retry the first 10 tasks and the other 5 that should be available")
    void testMarkTaskToRetry() {
        MutableFixedClock clock = new MutableFixedClock().withRealTimeStrategy();

        JdbcTaskInfoService jdbcTaskInfoService = new JdbcTaskInfoService(StorageResolver.IDENTITY, "partitionId", jdbcTemplate, clock, handlerLookup);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("polling 10 tasks");
        //TODO keep the order
        List<TaskInfo> taskTable = jdbcTaskInfoService.poll("task_table", 10);
        stopWatch.stop();
        printTasks(taskTable);
        assertEquals(10, taskTable.size());
        stopWatch.start("mark 10 tasks for retry in 60 seconds");
        Instant retryTime = Instant.now(clock).plusSeconds(60);
        log.info("retryTime={}", retryTime);
        taskTable.forEach(taskInfo -> jdbcTaskInfoService.markTaskToRetry(taskInfo, retryTime));
        stopWatch.stop();

        stopWatch.start("polling 2 tasks");
        List<TaskInfo> taskTable2 = jdbcTaskInfoService.poll("task_table", 10);
        stopWatch.stop();
        assertEquals(2, taskTable2.size());  // try to poll 10 but there are only 2 ready

        stopWatch.start("mark 2 tasks completed");
        taskTable2.forEach(taskInfo -> jdbcTaskInfoService.markTaskCompleted(taskInfo));
        stopWatch.stop();

        // No more tasks to process
        List<TaskInfo> taskTable3 = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(0, taskTable3.size());

        clock.increaseTime(Duration.ofSeconds(61)); // Increase time to make the tasks available

        stopWatch.start("polling 10 tasks");
        log.info("-------------------ERRROR--------------------------------");
        Pageable<TaskInfo> tasks = jdbcTaskInfoService.getTasks("task_table", ProcessStatus.PENDING, null, 30, Sort.ASC);
        assertNull(tasks.nextPageToken());
        printTasks(tasks);
        List<TaskInfo> taskTable4 = jdbcTaskInfoService.poll("task_table", 10);
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        assertEquals(10, taskTable4.size());
        taskTable4.forEach(taskInfo -> jdbcTaskInfoService.markTaskCompleted(taskInfo));

        Assertions.assertThat(taskTable4)
                .extracting(taskInfo -> new String(taskInfo.getPayloadBytes()))
                .contains("payload1", "payload2", "payload3", "payload4", "payload5", "payload6", "payload7", "payload8", "payload9", "payload10");

        stopWatch.start("polling 5 tasks");
        List<TaskInfo> taskTable5 = jdbcTaskInfoService.poll("task_table", 10);
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        assertEquals(5, taskTable5.size());
    }

    private void printTasks(Pageable<TaskInfo> tasks) {
        printTasks(tasks.results());
    }

    private void printTasks(List<TaskInfo> tasks) {
        tasks.forEach(taskInfo -> {
                    log.info("{}\t|{}\t|{}", taskInfo.getId(), taskInfo.getHandlerName(), taskInfo.getExecutionTime().toEpochMilli());
                }
        );
    }

    @Test
    @DisplayName("Polling 10 tasks then markTaskFailed all of them." +
            "Then try to get the failed tasks using the getTasks method and testing the pagination")
    void testMarkTaskFailed() {
        MutableFixedClock clock = new MutableFixedClock();

        JdbcTaskInfoService jdbcTaskInfoService = new JdbcTaskInfoService(StorageResolver.IDENTITY, "partitionId", jdbcTemplate, clock, handlerLookup);
        List<TaskInfo> taskTable = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(10, taskTable.size());

        taskTable.forEach(taskInfo -> jdbcTaskInfoService.markTaskFailed(taskInfo));

        Pageable<TaskInfo> pageable = jdbcTaskInfoService.getTasks("task_table", ProcessStatus.FAILED, null, 10, Sort.DESC);
        assertEquals(14, pageable.results().get(0).getId());
        assertEquals(3, pageable.results().get(9).getId());

        Pageable<TaskInfo> pageable1 = jdbcTaskInfoService.getTasks("task_table", ProcessStatus.FAILED, pageable.nextPageToken(), 10, Sort.DESC);
        assertEquals(2, pageable1.results().size());
        assertEquals(2, pageable1.results().get(0).getId());
        assertEquals(1, pageable1.results().get(1).getId());
        assertNull(pageable1.nextPageToken());
    }

    @Test
    @DisplayName("Get a task from the table, create an error and store it, then get the error")
    void testStoreError() {
        MutableFixedClock clock = new MutableFixedClock();

        JdbcTaskInfoService jdbcTaskInfoService = new JdbcTaskInfoService(StorageResolver.IDENTITY, "partitionId", jdbcTemplate, clock, handlerLookup);
        List<TaskInfo> taskTable = jdbcTaskInfoService.poll("task_table", 1);
        TaskInfoError errorMessage = taskTable.get(0)
                .createError(new RuntimeException("error message"));
        jdbcTaskInfoService.storeError(errorMessage);

        List<TaskInfoError> taskTable1 = jdbcTaskInfoService.getTaskErrors("task_table", taskTable.get(0).getId());

        Assertions.assertThat(taskTable1)
                .extracting(TaskInfoError::getErrorMessage)
                .contains("error message");
    }

    @Test
    @DisplayName("""
            There are 12 pending tasks, poll 6 tasks now I should have 6 in processing status
            Then poll 4 tasks, now I should have 4 in processing status
            then wait 5 seconds the task will expire to process in 3 seconds and 
            poll 10 tasks, only 2 should be available.
            Keep in mind that the other pending tasks become available after the 8 seconds. See task-data.sql""")
    void testProcessingExpiration() {
        MutableFixedClock clock = new MutableFixedClock();

        JdbcTaskInfoService jdbcTaskInfoService = new JdbcTaskInfoService(StorageResolver.IDENTITY, "partitionId", jdbcTemplate, clock, handlerLookup);

        List<TaskInfo> taskTable = jdbcTaskInfoService.poll("task_table", 6);
        assertEquals(6, taskTable.size());
        log.info("-------------------------------------------------");

        List<TaskInfo> taskTable3 = jdbcTaskInfoService.poll("task_table", 4);
        assertEquals(4, taskTable3.size());
        log.info("-------------------------------------------------");

        clock.increaseTime(Duration.ofSeconds(5));
        log.info("-----------plus 5 minutes------------");
        List<TaskInfo> taskTable4 = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(2, taskTable4.size());
    }


    @Test
    @DisplayName("""
            When no update still check for processing tasks""")
    void testProcessingWhenNoUpdate() {
        MutableFixedClock clock = new MutableFixedClock();

        JdbcTaskInfoService jdbcTaskInfoService = new JdbcTaskInfoService(StorageResolver.IDENTITY, "partitionId", jdbcTemplate, clock, handlerLookup);

        List<TaskInfo> taskTable = jdbcTaskInfoService.poll("task_table", 12);
        assertEquals(12, taskTable.size());

        List<TaskInfo> taskTable3 = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(0, taskTable3.size());

    }

    @Test
    @DisplayName("""
            Polling 10 tasks then try to poll 10 more tasks
            but only 2 tasks are available then wait for 10 seconds
            All the tasks 12 should be expired """)
    void testRetrieveProcessingTimeoutTasks() {
        MutableFixedClock clock = new MutableFixedClock();

        JdbcTaskInfoService jdbcTaskInfoService = new JdbcTaskInfoService(StorageResolver.IDENTITY, "partitionId", jdbcTemplate, clock, handlerLookup);

        List<TaskInfo> taskTable = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(10, taskTable.size());

        List<TaskInfo> taskTable2 = jdbcTaskInfoService.poll("task_table", 10);
        assertEquals(2, taskTable2.size());

        clock.increaseTime(Duration.ofSeconds(10));
        List<TaskInfo> taskTable3 = jdbcTaskInfoService.retrieveProcessingTimeoutTasks("task_table");
        assertEquals(12, taskTable3.size());
    }

    @Test
    @DisplayName("""
            Polling 12 tasks then try to poll 20 more tasks
            but there is nothing to poll 
            then wait for 10 seconds
            then poll 20 tasks and the last 5 are availables
            then wait for 10 seconds
            Now check that nothing can be polled
            Now All the tasks 17 should be expired ,
            then requeue the timeout tasks""")
    void testRequeueTimeoutTask() {
        MutableFixedClock clock = new MutableFixedClock();

        JdbcTaskInfoService jdbcTaskInfoService = new JdbcTaskInfoService(StorageResolver.IDENTITY, "partitionId", jdbcTemplate, clock, handlerLookup);

        List<TaskInfo> taskTable = jdbcTaskInfoService.poll("task_table", 20);
        assertEquals(12, taskTable.size());
        List<TaskInfo> taskTable2 = jdbcTaskInfoService.poll("task_table", 20);
        assertEquals(0, taskTable2.size());

        clock.increaseTime(Duration.ofSeconds(10));
        List<TaskInfo> taskTable3 = jdbcTaskInfoService.poll("task_table", 20);
        assertEquals(5, taskTable3.size());
        clock.increaseTime(Duration.ofSeconds(10));

        List<TaskInfo> taskTable4 = jdbcTaskInfoService.poll("task_table", 20);
        assertEquals(0, taskTable4.size());

        jdbcTaskInfoService.requeueTimeoutTask("task_table");

        List<TaskInfo> taskTable5 = jdbcTaskInfoService.poll("task_table", 20);
        assertEquals(17, taskTable5.size());
    }
}