package com.pcistudio.contact.manager;

import com.pcistudio.processor.test.handler.RandomTaskHandler;
import com.pcistudio.processor.test.handler.TaskInfoServiceTestHelper;
import com.pcistudio.processor.test.handler.TaskProcessorTest;
import com.pcistudio.processor.test.writer.TaskWriterHelper;
import com.pcistudio.processor.test.writer.TaskWriterTest;
import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.handler.*;
import com.pcistudio.task.procesor.page.Pageable;
import com.pcistudio.task.procesor.page.PageableReader;
import com.pcistudio.task.procesor.page.Sort;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.util.JsonUtil;
import com.pcistudio.task.procesor.writer.TaskWriter;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
import com.pcistudio.task.processor.config.TaskProcessorManagerCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@TaskProcessorTest
@TaskWriterTest
@TestPropertySource(properties = {
//        "debug=true",
        "spring.task.logging.template=true",
        "logging.level.com.pcistudio.task.procesor.template.LoggingJdbcTemplate=DEBUG",
        "logging.level.org.springframework.jdbc.core.JdbcTemplate=INFO",
        "logging.level.com.pcistudio=INFO",
        "task.processor.metrics.enable=true"
})
class WriteAndProcessTest {
    static RandomTaskHandler<Person> personRandomTaskHandler = new RandomTaskHandler<Person>()
            .builder()
            .withConsumer(person -> {
                log.info("{}", person);
            })
            .withTaskCount(50)
            .withExpectedException(new RuntimeException(), 1)
            .withExpectedException(new TaskTransientException(""), 3)
            .withExpectedException(new TaskHandlerTransientException(""), 3)
            .withExpectedException(new IllegalArgumentException(""), 3)
            .withStopErrorsCallsAfter(Duration.ofMinutes(3))
            .withStopSlowCallsAfter(Duration.ofMinutes(2))
            .build();

    @Configuration
    static class TestHandlerConfiguration extends AbstractHandlersConfiguration {
        @Override
        protected void addTask(HandlerManagerImpl.Builder builder) {
            builder.register(HandlerProperties.builder()
                    .handlerName("test_one")
                    .tableName("test_one")
                    .pollInterval(5000)
                    .requeueInterval(10000)
                    .transientExceptions(Set.of(TransientDataAccessException.class))
                    .taskHandler(payload -> handle((Person) payload))
                    .taskHandlerType(Person.class)
                    .build());

            builder.register(HandlerProperties.builder()
                    .handlerName("test_one_hundred")
                    .tableName("test_one_hundred")
                    .pollInterval(5000)
                    .requeueInterval(10000)
                    .transientExceptions(Set.of(TransientDataAccessException.class))
                    .taskHandler(payload -> handle((Person) payload))
                    .taskHandlerType(Person.class)
                    .build());

            builder.register(HandlerProperties.builder()
                    .handlerName("test_one_thousand")
                    .tableName("test_one_thousand")
                    .pollInterval(5000)
                    .requeueInterval(10000)
                    .maxParallelTasks(10)
                    .transientExceptions(Set.of(TransientDataAccessException.class))
                    .taskHandler(personRandomTaskHandler)
                    .taskHandlerType(Person.class)
                    .build());
        }

        @Bean
        TaskProcessorManagerCustomizer taskProcessorManagerCustomizer(ObjectProvider<TaskInfoVisibilityService> taskInfoServicesProvider) {
            return taskProcessorManager1 -> {
                taskProcessorManager1.getEventPublisher("test_one")
                        .onRequeueEnded(requeueEvent -> {
                            log.info("Requeue listener event handlerName={}, success={}", requeueEvent.handlerName(), requeueEvent.success());
                            TaskInfoVisibilityService taskInfoService = taskInfoServicesProvider.getIfUnique();
                            Pageable<TaskInfo> tasks = taskInfoService.getTasks("test_one", ProcessStatus.PROCESSING, null, 10, Sort.ASC);
                            JsonUtil.print("Tasks view", tasks);
                        });
            };
        }
    }

    @Autowired
    private TaskWriter taskWriter;

    @Autowired
    private TaskInfoService taskInfoService;

    @Autowired
    private TaskProcessorLifecycleManager taskProcessorManager;

    @Autowired
    private TaskWriterHelper taskWriterHelper;

    @Autowired
    TaskInfoServiceTestHelper taskInfoServiceTestHelper;

    @Test
    void writeOne() throws InterruptedException, ExecutionException {

        taskWriterHelper
                .writeSyncData("test_one", 1, () -> new Person("Hello World", 31))
                .get();

        taskProcessorManager.start("test_one");
        Thread.sleep(10000);
        Map<String, Integer> stats = ((TaskProcessorManager) taskProcessorManager).stats("test_one");

        assertEquals(1, stats.get(ProcessStatus.COMPLETED.name()));
        taskProcessorManager.close("test_one");
    }

    @Test
    void writeOneHundred() throws InterruptedException, ExecutionException {

        taskWriterHelper
                .writeSyncData("test_one_hundred", 100, () -> new Person("Hello World", 31))
                .get();

        taskProcessorManager.start("test_one_hundred");

        taskInfoServiceTestHelper.waitForTask("test_one_hundred");

        Map<String, Integer> stats = ((TaskProcessorManager) taskProcessorManager).stats("test_one_hundred");

        assertEquals(100, stats.get(ProcessStatus.COMPLETED.name()));
        taskProcessorManager.close("test_one_hundred");
    }

    @Test
    void write1000() throws InterruptedException, ExecutionException {

        taskWriterHelper
                .writeSyncData("test_one_thousand", 1000, () -> new Person("Hello World", 31))
                .get();

        taskProcessorManager.start("test_one_thousand");

        taskInfoServiceTestHelper.waitForTask("test_one_thousand");

        taskInfoServiceTestHelper.printStats("test_one_thousand");

        PageableReader<TaskInfo> pageableReader = new PageableReader<>(s -> taskInfoService.getTasksRetried("test_one_thousand", s, 100));

        taskInfoServiceTestHelper.printTasks(pageableReader.readAll());

        Map<String, Integer> stats = ((TaskProcessorManager) taskProcessorManager).stats("test_one_thousand");

        Assertions.assertThat(stats.get(ProcessStatus.COMPLETED.name())).isGreaterThan(850);

        taskProcessorManager.close("test_one_thousand");
    }

    record Person(String name, int age) {
    }

    public static void handle(Person person) {
        log.info("name={}, age={}", person.name, person.age);
    }
}







