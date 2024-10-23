package com.pcistudio.contact.manager;

import com.pcistudio.task.procesor.task.TaskParams;
import com.pcistudio.task.procesor.writer.TaskWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"debug=true", "spring.task.processor.writer.enabled=true"})
class MainTest {

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