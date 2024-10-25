package com.pcistudio.contact.manager;

import com.pcistudio.processor.test.handler.TaskProcessorTest;
import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.handler.TaskProcessorLifecycleManager;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static java.util.concurrent.locks.LockSupport.park;

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
                    .build());
        }
    }

    @Autowired
    private TaskProcessorLifecycleManager taskProcessorManager;

    @Test
    void writeHelloWorld() {
        taskProcessorManager.start();
//        park();//
    }
}







