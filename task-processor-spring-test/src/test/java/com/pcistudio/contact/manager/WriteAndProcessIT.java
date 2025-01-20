package com.pcistudio.contact.manager;

import com.pcistudio.processor.test.handler.RandomTaskHandler;
import com.pcistudio.processor.test.handler.TaskInfoServiceTestHelper;
import com.pcistudio.processor.test.handler.TaskProcessorTest;
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
import java.util.Set;
import java.util.concurrent.ExecutionException;

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

    //    static RandomTaskHandler<Person> personRandomTaskHandlerWithSlowCalls = new RandomTaskHandler<Person>()
//            .builder()
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

    @Configuration
    static class TestHandlerConfiguration extends AbstractHandlersConfiguration {
        @Override
        protected void addTask(HandlerManagerImpl.Builder builder) {
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
            //TODO: One for circuit breaker
            // and one that fails and pause the processor
            // also test the slow task
            // execution time out occur and the taskis already executing
        }

        @Bean
        TaskProcessorManagerCustomizer taskProcessorManagerCustomizer(ObjectProvider<TaskInfoVisibilityService> taskInfoServicesProvider) {
            return taskProcessorManager1 -> {
                taskProcessorManager1.getEventPublisher("test_slow_calls")
                        .onCircuitBreakerWaiting(requeueEvent -> {
                            log.info("CircuitBreakerWaitingEvent in handlerName={}", requeueEvent.handlerName());
                        });
            };
        }

        @Bean
        CircuitBreakerDecorator circuitBreakerDecorator() {
            return new DefaultCircuitBreakerDecorator(CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                    .failureRateThreshold(50)                        // Set failure threshold to 50%
                    .slowCallRateThreshold(50)                       // Set slow call threshold to 50%
                    .waitDurationInOpenState(Duration.ofSeconds(5)) // Wait 60s before transitioning from OPEN to HALF_OPEN
                    .slowCallDurationThreshold(Duration.ofSeconds(2))// Consider calls slower than 2s as "slow"
                    .permittedNumberOfCallsInHalfOpenState(5)        // Allow 5 calls when in HALF_OPEN state
                    .minimumNumberOfCalls(10)                        // Require at least 10 calls to calculate failure rate
                    .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // Use COUNT_BASED or TIME_BASED window
                    .slidingWindowSize(20)                           // Window size for recording call metrics
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
        //TODO: One for circuit breaker
        // and one that fails and pause the processor
        // also test the slow task
        // execution time out occur and the tasks already executing
    void writeSlowCalls() throws InterruptedException, ExecutionException {

        taskWriterHelper
                .writeSyncData("test_slow_calls", 1000, () -> new Person("Hello World", 31))
                .get();

        taskProcessorManager.start("test_slow_calls");

        taskInfoServiceTestHelper.waitForTask("test_slow_calls");

        taskInfoServiceTestHelper.printStats("test_slow_calls");

        PageableReader<TaskInfo> pageableReader = new PageableReader<>(s -> taskInfoService.getTasksRetried("test_slow_calls", s, 100));

        taskInfoServiceTestHelper.printTasks(pageableReader.readAll());

        Map<String, Integer> stats = ((TaskProcessorManager) taskProcessorManager).stats("test_slow_calls");

        Assertions.assertThat(stats.get(ProcessStatus.COMPLETED.name())).isGreaterThan(850);

        taskProcessorManager.close("test_slow_calls");
    }

    private record Person(String name, int age) {
    }

}







