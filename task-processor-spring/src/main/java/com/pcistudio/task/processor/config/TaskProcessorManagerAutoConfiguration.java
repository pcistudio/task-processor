package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.JdbcTaskInfoService;
import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.handler.*;
import com.pcistudio.task.procesor.metrics.TaskProcessorMetricsFactory;
import com.pcistudio.task.procesor.register.HandlerLookup;
import com.pcistudio.task.procesor.util.decoder.MessageDecoding;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;

@ConditionalOnClass(name = "com.pcistudio.task.procesor.JdbcTaskInfoService")
@Configuration
@Import({DecodingConfiguration.class, MetricsConfig.class})
@ConditionalOnProperty(prefix = "spring.task.handlers", name = "enabled", havingValue = "true")
public class TaskProcessorManagerAutoConfiguration {

    private final String partitionId;

    public TaskProcessorManagerAutoConfiguration(
            @Value("${task.processor.partitionId:#{T(java.util.UUID).randomUUID().toString()}}") String partitionId) {
        this.partitionId = partitionId;
    }

    @Bean
    @ConditionalOnMissingBean
    TaskInfoService taskInfoService(StorageResolver storageResolver, JdbcTemplate jdbcTemplate, Clock clock, HandlerLookup handlerLookup) {
        return new JdbcTaskInfoService(storageResolver, partitionId, jdbcTemplate, clock, handlerLookup);
    }

    @Bean
    @ConditionalOnMissingBean
    TaskProcessorLifecycleManager taskProcessorManager(
            HandlerLookup handlerLookup,
            TaskInfoService taskInfoService,
            MessageDecoding messageDecoding,
            Clock clock,
            TaskProcessorMetricsFactory taskProcessorMetricsFactory,
            ObjectProvider<CircuitBreakerDecorator> circuitBreakerDecoratorProvider,
            ObjectProvider<TaskProcessorManagerCustomizer> taskProcessorManagerCustomizerProvider
    ) throws BeansException {
        TaskProcessorManager taskProcessorManager = new TaskProcessorManager(taskProcessorMetricsFactory);

        // Error if I going to put this listener to every body then the handler name don't make sense
        // the other thing is how the event is propagated to new added task handlers
        handlerLookup.getIterator().forEachRemaining(properties -> {
            TaskProcessingContext context = TaskProcessingContext.builder()
                    .handlerProperties(properties)
                    .taskInfoService(taskInfoService)
                    .retryManager(
                            properties.isExponentialBackoff()
                                    ? new ExponentialRetryStrategy(properties.getRetryDelayMs(), properties.getMaxRetries(), clock)
                                    : new FixRetryStrategy(properties.getRetryDelayMs(), properties.getMaxRetries(), clock)
                    )
                    .clock(clock)
                    .messageDecoding(messageDecoding)
                    .taskHandler(properties.getTaskHandler())
                    .circuitBreakerDecorator(circuitBreakerDecoratorProvider.getIfAvailable())
                    .build();
            taskProcessorManager.createTaskProcessor(context);
        });
        taskProcessorManagerCustomizerProvider
                .ifAvailable(taskProcessorManagerCustomizer -> taskProcessorManagerCustomizer.customize(taskProcessorManager));
        return taskProcessorManager;
    }
}
