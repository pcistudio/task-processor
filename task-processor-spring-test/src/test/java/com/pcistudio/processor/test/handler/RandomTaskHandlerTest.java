package com.pcistudio.processor.test.handler;

import com.pcistudio.task.procesor.handler.TaskHandlerTransientException;
import com.pcistudio.task.procesor.handler.TaskTransientException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class RandomTaskHandlerTest {

    @Test
    void process() {
        RandomTaskHandler<Person> personRandomTaskHandler = new RandomTaskHandler<Person>()
                .builder()
                .withConsumer(person -> {
                    log.info("{}", person);
                })
                .withTaskCount(10)
                .withExpectedException(new RuntimeException(), 1)
                .withExpectedException(new TaskTransientException(""), 3)
                .withExpectedException(new TaskHandlerTransientException(""), 3)
                .withExpectedException(new IllegalArgumentException(""), 3)
                .enableDebugConfiguration()
                .build();

        for (int i = 0; i < 10; i++) {
            try {
                personRandomTaskHandler.process(new Person("Person " + i, i));
            } catch (Exception e) {
            }
        }

        personRandomTaskHandler.assertExceptionCount(RuntimeException.class, 1);
        personRandomTaskHandler.assertExceptionCount(TaskTransientException.class, 3);
        personRandomTaskHandler.assertExceptionCount(TaskHandlerTransientException.class, 3);
        personRandomTaskHandler.assertExceptionCount(IllegalArgumentException.class, 3);
        personRandomTaskHandler.printStats();
    }

    @Test
    void process50() {
        RandomTaskHandler<Person> personRandomTaskHandler = new RandomTaskHandler<Person>()
                .builder()
                .withConsumer(person -> {
                    log.info("{}", person);
                })
                .withTaskCount(50)
                .withExpectedException(new RuntimeException(), 1)
                .withExpectedException(new TaskTransientException(""), 3)
                .withExpectedException(new TaskHandlerTransientException(""), 3)
                .withExpectedException(new IllegalArgumentException(""), 3)
                .withSlowTaskCount(5)
                .withSlowTaskDurationMs(10)
                .enableDebugConfiguration()
                .build();

        for (int i = 0; i < 50; i++) {
            try {
                personRandomTaskHandler.process(new Person("Person " + i, i));
            } catch (Exception e) {
            }
        }

        personRandomTaskHandler.assertExceptionCount(RuntimeException.class, 1);
        personRandomTaskHandler.assertExceptionCount(TaskTransientException.class, 3);
        personRandomTaskHandler.assertExceptionCount(TaskHandlerTransientException.class, 3);
        personRandomTaskHandler.assertExceptionCount(IllegalArgumentException.class, 3);
        personRandomTaskHandler.assertExceptionCount(IllegalArgumentException.class, 3);
        personRandomTaskHandler.assertSlowCount(5);
        personRandomTaskHandler.printStats();
    }

    record Person(String name, int age) {
    }
}