package com.pcistudio.contact.manager;

import com.pcistudio.processor.test.handler.TaskProcessorTest;
import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.handler.TaskProcessorLifecycleManager;
import com.pcistudio.task.procesor.handler.TaskProcessorManager;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
import com.pcistudio.task.processor.config.TaskProcessorManagerCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

@TaskProcessorTest
@TestPropertySource(properties = {"debug=true"})
class TestTaskProcessorTestAnnotation {
    @Configuration
    static class TestHandlerConfiguration extends AbstractHandlersConfiguration {
        @Override
        protected void addTask(HandlerManagerImpl.Builder builder) {
            builder.register(HandlerProperties.builder()
                    .handlerName("test")
                    .tableName("test")
                    .transientExceptions(Set.of(TransientDataAccessException.class))
                    .taskHandler(payload -> {
                        System.out.println("test");
                    })
                    .requeueInterval(30000)
                    .taskHandlerType(Object.class)
                    .build());
        }

        @Bean
        TaskProcessorManagerCustomizer taskProcessorManagerCustomizer() {
            return new TaskProcessorManagerCustomizerImpl();
        }
    }

    @Slf4j
    static class TaskProcessorManagerCustomizerImpl implements TaskProcessorManagerCustomizer {
        public void customize(TaskProcessorManager taskProcessorManager) {
            taskProcessorManager.getEventPublisher("test")
                    .onRequeueEnded(requeueEndedEvent -> log.info("Requeue handlerName={}, success={}, count={}",
                            requeueEndedEvent.handlerName(), requeueEndedEvent.success(), requeueEndedEvent.requeueCount()));
        }

    }

    @Autowired
    private TaskProcessorLifecycleManager taskProcessorManager;

    @Test
    void writeHelloWorld()  {

        taskProcessorManager.start();
//        park();
    }
}







