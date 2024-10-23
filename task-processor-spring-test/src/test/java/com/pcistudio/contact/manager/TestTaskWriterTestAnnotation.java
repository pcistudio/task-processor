package com.pcistudio.contact.manager;

import com.pcistudio.processor.test.writer.TaskWriterTest;
import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.procesor.task.TaskParams;
import com.pcistudio.task.procesor.writer.TaskWriter;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@TaskWriterTest
@TestPropertySource(properties = {"debug=true"})
class TestTaskWriterTestAnnotation {
    @Configuration
    static class TestHandlerConfiguration extends AbstractHandlersConfiguration {
        @Override
        protected void addTask(HandlerManagerImpl.Builder builder) {
            builder.register(HandlerProperties.builder()
                    .handlerName("test")
                    .tableName("test")
                    .taskHandler(payload -> {
                        System.out.println("test");
                    })
                    .build());
        }
    }

    @Autowired
    private TaskWriter taskWriter;

    @Test
    void writeHelloWorld() {
        taskWriter.writeTasks(TaskParams.builder()
                .handlerName("test")
                .payload("Hello World")
                .build());
    }

    @Test
    void writeHelloWorldJson() {

        taskWriter.writeTasks(TaskParams.builder()
                .handlerName("test")
                .payload(new Person("Hello World", 31))
                .build());
    }

    record Person(String name, int age) {
    }
}