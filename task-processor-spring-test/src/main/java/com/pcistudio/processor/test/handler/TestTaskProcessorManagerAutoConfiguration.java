package com.pcistudio.processor.test.handler;

import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration(before = {
        com.pcistudio.task.processor.config.TaskProcessorManagerAutoConfiguration.class
})
public class TestTaskProcessorManagerAutoConfiguration {


}
