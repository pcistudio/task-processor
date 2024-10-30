package com.pcistudio.contact.manager;

import com.pcistudio.processor.test.handler.TaskProcessorTest;
import com.pcistudio.processor.test.writer.TaskWriterTest;
import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.handler.RequeueListener;
import com.pcistudio.task.procesor.handler.TaskInfoService;
import com.pcistudio.task.procesor.handler.TaskInfoVisibilityService;
import com.pcistudio.task.procesor.handler.TaskProcessorLifecycleManager;
import com.pcistudio.task.procesor.page.Pageable;
import com.pcistudio.task.procesor.page.Sort;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.procesor.task.TaskParams;
import com.pcistudio.task.procesor.util.JsonUtil;
import com.pcistudio.task.procesor.writer.TaskWriter;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.concurrent.locks.LockSupport.park;

@Slf4j
@TaskProcessorTest
@TaskWriterTest
@TestPropertySource(properties = {
        "debug=true",
        "spring.task.logging.template=true",
        "logging.level.org.springframework.jdbc.core.JdbcTemplate=DEBUG",
        "logging.level.com.pcistudio=TRACE",

})
class WriteAndProcessTest {
    @Configuration
    static class TestHandlerConfiguration extends AbstractHandlersConfiguration {
        @Override
        protected void addTask(HandlerManagerImpl.Builder builder) {
            builder.register(HandlerProperties.builder()
                    .handlerName("test")
                    .tableName("test")
                    .pollInterval(5000)
                    .requeueInterval(10000)
                    .transientExceptions(Set.of(TransientDataAccessException.class))
                    .taskHandler(payload -> handle((Person) payload))
                    .build());
        }

        @Bean
        List<RequeueListener> requeueListenerList(ObjectProvider<TaskInfoVisibilityService> taskInfoServicesProvider) {
            return List.of(
                    requeueEvent -> {
                        log.info("Requeue listener event handlerName={}, success={}", requeueEvent.getHandlerName(), requeueEvent.isSuccess());
                        TaskInfoVisibilityService taskInfoService = taskInfoServicesProvider.getIfUnique();
                        Pageable<TaskInfo> tasks = taskInfoService.getTasks("test", ProcessStatus.PROCESSING, null, 10, Sort.ASC);
                        JsonUtil.print("Tasks view", tasks);
                    }
            );
        }
    }

    @Autowired
    private TaskWriter taskWriter;

    @Autowired
    private TaskInfoService taskInfoService;

    @Autowired
    private TaskProcessorLifecycleManager taskProcessorManager;

    @Test
    void writeHelloWorld() {
        Thread thread = new Thread(this::writeData);
        thread.start();
        taskProcessorManager.start();
        park();
    }

    private void writeData() {
        Stream.iterate(1, integer -> integer + 1)
                        .limit(1)
                                .forEach(integer -> {
                                    taskWriter.writeTasks(TaskParams.builder()
                                            .handlerName("test")
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







