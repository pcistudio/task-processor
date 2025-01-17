package com.pcistudio.processor.test.handler;

import com.pcistudio.task.procesor.handler.TaskInfoService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@AutoConfiguration(after = {
        com.pcistudio.task.processor.config.TaskProcessorManagerAutoConfiguration.class
})
public class TestTaskProcessorManagerAutoConfiguration {

    @Bean
    TaskInfoServiceTestHelper taskInfoServiceHelper(TaskInfoService taskInfoService, Clock clock) {
        return new TaskInfoServiceTestHelper(taskInfoService, clock);
    }
}
