package com.pcistudio.contact.manager;

import com.pcistudio.processor.test.handler.TaskProcessorTest;
import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.handler.RequeueListener;
import com.pcistudio.task.procesor.handler.TaskProcessorLifecycleManager;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
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
//@ComponentScan
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
                    .build());
        }

        @Bean
        TestListener testListener() {
            return new TestListener();
        }
    }

    @Autowired
    private TaskProcessorLifecycleManager taskProcessorManager;

    @Test
    void writeHelloWorld() {

        taskProcessorManager.start();
//        park();
    }

    @Slf4j
    static class TestListener implements RequeueListener {

        public TestListener() {
            log.info("Creating TestListener");
        }

        @Override
        public void requeued(RequeueEvent requeueEvent) {
            log.info("RequeueEvent");
        }
    }
}







