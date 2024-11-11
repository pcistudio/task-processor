package com.pcistudio.contact.manager;

import com.pcistudio.processor.test.handler.TaskProcessorTest;
import com.pcistudio.processor.test.writer.TaskWriterTest;
import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.handler.TaskInfoService;
import com.pcistudio.task.procesor.handler.TaskInfoVisibilityService;
import com.pcistudio.task.procesor.handler.TaskProcessorLifecycleManager;
import com.pcistudio.task.procesor.handler.TaskProcessorManager;
import com.pcistudio.task.procesor.page.Pageable;
import com.pcistudio.task.procesor.page.Sort;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskParams;
import com.pcistudio.task.procesor.util.JsonUtil;
import com.pcistudio.task.procesor.writer.TaskWriter;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
import com.pcistudio.task.processor.config.TaskProcessorManagerCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@TaskProcessorTest
@TaskWriterTest
@TestPropertySource(properties = {
//        "debug=true",
//        "spring.task.logging.template=true",
        "logging.level.org.springframework.jdbc.core.JdbcTemplate=INFO",
        "logging.level.com.pcistudio=INFO",
//
})
class WriteAndProcessTest {
    private final static CountDownLatch latchProcessing = new CountDownLatch(1);
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

//                        Pageable<TaskInfo> tasks = taskInfoService.getTasks("test", ProcessStatus., null, 10, Sort.ASC);
//                        JsonUtil.print("Tasks view", tasks);
                        });

                taskProcessorManager1.getEventPublisher("test_one_hundred")
                        .onRequeueEnded(requeueEvent -> {
                            log.info("Requeue listener event handlerName={}, success={}", requeueEvent.handlerName(), requeueEvent.success());
                            TaskInfoVisibilityService taskInfoService = taskInfoServicesProvider.getIfUnique();
                            Pageable<TaskInfo> tasks = taskInfoService.getTasks("test_one_hundred", ProcessStatus.PROCESSING, null, 10, Sort.ASC);
                            JsonUtil.print("Tasks view", tasks);

                        });
//                taskProcessorManager1.getEventPublisher("test_one_hundred")
//                        .onProcessingBatch(requeueEvent -> {
//                            log.info("countDown latch");
//                            latchProcessing.countDown();
//                        });
                taskProcessorManager1.getEventPublisher("test_one_hundred")
                        .onPollWaitingEnded(requeueEvent -> {
                            log.info("countDown latch");
                            latchProcessing.countDown();
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

    @Test
    void writeOne() throws InterruptedException {

        Thread thread = new Thread(() -> writeData("test_one", 1));
        thread.start();
        taskProcessorManager.start("test_one");
        Thread.sleep(10000);
        Map<ProcessStatus, Integer> stats = ((TaskProcessorManager) taskProcessorManager).stats("test_one");

        assertEquals(1, stats.get(ProcessStatus.COMPLETED));
        taskProcessorManager.close("test_one");
    }

    @Test
    void writeOneHundred() throws InterruptedException {
        Thread thread = new Thread(() -> writeData("test_one_hundred", 100));
        thread.start();
        thread.join();
        taskProcessorManager.start("test_one_hundred");

        latchProcessing.await();
        Map<ProcessStatus, Integer> stats = ((TaskProcessorManager) taskProcessorManager).stats("test_one_hundred");

        assertEquals(100, stats.get(ProcessStatus.COMPLETED));
        taskProcessorManager.close("test_one_hundred");
    }

    private void writeData(String handlerName, int limit) {
        Stream.iterate(1, integer -> integer + 1)
                .limit(limit)
                .forEach(integer -> {
                    taskWriter.writeTasks(TaskParams.builder()
                            .handlerName(handlerName)
                            .payload(new Person("Hello World", 31))
                            .build());
                });
        log.info("\n-------------------------------------------------");
    }


    record Person(String name, int age) {
    }


    public static void handle(Person person) {
        log.info("name={}, age={}", person.name, person.age);
    }
}







