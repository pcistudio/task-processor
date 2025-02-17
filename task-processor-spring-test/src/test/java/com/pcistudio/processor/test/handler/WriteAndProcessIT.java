package com.pcistudio.processor.test.handler;

import com.pcistudio.processor.test.writer.TaskWriterHelper;
import com.pcistudio.processor.test.writer.TaskWriterTest;
import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.handler.*;
import com.pcistudio.task.procesor.page.PageableReader;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.procesor.task.ProcessStatus;
import com.pcistudio.task.procesor.task.TaskInfo;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
import com.pcistudio.task.processor.config.TaskProcessorManagerCustomizer;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
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
import java.util.Set;import java.util.concurrent.atomic.AtomicInteger;

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
class WriteAndProcessIT {

    static RandomTaskHandler<Person> personRandomTaskHandlerWithSlowCalls = RandomTaskHandler.<Person>builder()
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
            .withSlowTaskCount(26)
            .withSlowTaskDurationMs(2001)
            .build();

    static AtomicInteger circuitOpenCount = new AtomicInteger(0);

    @Configuration
    static class TestHandlerConfiguration extends AbstractHandlersConfiguration {
        @Override
        protected void configureHandler(HandlerManagerImpl.Builder builder) {
            builder.register(HandlerProperties.builder()
                    .handlerName("test_slow_calls")
                    .tableName("test_slow_calls")
                    .pollInterval(5000)
                    .requeueInterval(10000)
                    .maxParallelTasks(10)
                    .transientExceptions(Set.of(TransientDataAccessException.class))
                    .taskHandler(personRandomTaskHandlerWithSlowCalls)
                    .taskHandlerType(Person.class)
                    .build());
        }

        @Bean
        TaskProcessorManagerCustomizer taskProcessorManagerCustomizer(ObjectProvider<TaskInfoVisibilityService> taskInfoServicesProvider) {
            return taskProcessorManager1 -> {
                taskProcessorManager1.getEventPublisher("test_slow_calls")
                        .onCircuitBreakerWaiting(requeueEvent -> {
                            log.info("CircuitBreakerWaitingEvent in handlerName={}", requeueEvent.handlerName());
                            circuitOpenCount.incrementAndGet();

                        });
            };
        }

        @Bean
        CircuitBreakerDecorator circuitBreakerDecorator() {
            return new DefaultCircuitBreakerDecorator(CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                    .failureRateThreshold(50)
                    .slowCallRateThreshold(50)
                    .waitDurationInOpenState(Duration.ofSeconds(5))
                    .slowCallDurationThreshold(Duration.ofSeconds(2))
                    .permittedNumberOfCallsInHalfOpenState(5)
                    .minimumNumberOfCalls(10)
                    .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                    .slidingWindowSize(20)
                    .build()));
        }
    }

    @Autowired
    private TaskInfoService taskInfoService;

    @Autowired
    private TaskProcessorLifecycleManager taskProcessorManager;

    @Autowired
    private TaskWriterHelper taskWriterHelper;

    @Autowired
    TaskInfoServiceTestHelper taskInfoServiceTestHelper;

    @Test
    void writeSlowCalls() throws InterruptedException {

        taskWriterHelper
                .writeData("test_slow_calls", 1000, () -> new Person("Hello World", 31));

        taskProcessorManager.start("test_slow_calls");

        taskInfoServiceTestHelper.waitForTask("test_slow_calls");

        taskInfoServiceTestHelper.printStats("test_slow_calls");

        PageableReader<TaskInfo> pageableReader = new PageableReader<>(s -> taskInfoService.getTasksRetried("test_slow_calls", s, 100));

        taskInfoServiceTestHelper.printTasks(pageableReader.readAll());

        Map<String, Integer> stats = ((TaskProcessorManager) taskProcessorManager).todayStats("test_slow_calls");

        Assertions.assertThat(stats.get(ProcessStatus.COMPLETED.name())).isGreaterThan(850);

        taskProcessorManager.close("test_slow_calls");

        Assertions.assertThat(circuitOpenCount.get()).isPositive();
    }

    private record Person(String name, int age) {
    }

}







