package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.JdbcTaskInfoService;
import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.handler.*;
import com.pcistudio.task.procesor.register.HandlerLookup;
import com.pcistudio.task.procesor.util.decoder.MessageDecoding;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.util.List;

@ConditionalOnClass(name = "com.pcistudio.task.procesor.JdbcTaskInfoService")
@Configuration
@Import(DecodingConfiguration.class)
@ConditionalOnProperty(prefix = "spring.task.handlers", name = "enabled", havingValue = "true")
public class TaskProcessorManagerAutoConfiguration {

    @Value("${task.processor.partitionId:#{T(java.util.UUID).randomUUID().toString()}}")
    private String partitionId;

    @Bean
    TaskInfoService taskInfoService(StorageResolver storageResolver, JdbcTemplate jdbcTemplate, Clock clock, HandlerLookup handlerLookup) {
        return new JdbcTaskInfoService(storageResolver, partitionId, jdbcTemplate, clock, handlerLookup);
    }

    @ConditionalOnMissingBean(value = Clock.class)
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public TaskProcessorLifecycleManager taskProcessorManager(
            HandlerLookup handlerLookup,
            TaskInfoService taskInfoService,
            MessageDecoding messageDecoding,
            List<RequeueListener> requeueListeners) throws BeansException {

        TaskProcessorManager taskProcessorManager = new TaskProcessorManager();

        // Error if I going to put this listener to every body then the handler name dont make sence
        // the ither thing is how the event is propagated to new added task handlers
        handlerLookup.getIterator().forEachRemaining(properties -> {
            TaskProcessingContext context = TaskProcessingContext.builder()
                    .handlerProperties(properties)
                    .taskInfoService(taskInfoService)
//                    .listeners(requeueListeners.getIfAvailable(ArrayList::new))
                    .listeners(requeueListeners)
                    .retryManager(
                            properties.isExponentialBackoff()
                                    ? new ExponentialRetryManager(properties.getRetryDelayMs(), properties.getMaxRetries(), clock())
                                    : new FixRetryManager(properties.getRetryDelayMs(), properties.getMaxRetries(), clock())
                    )
                    .clock(clock())
                    .messageDecoding(messageDecoding)
                    .taskHandler(properties.getTaskHandler())
                    .build();
            taskProcessorManager.createTaskProcessor(context);
        });

        return taskProcessorManager;
    }
}
