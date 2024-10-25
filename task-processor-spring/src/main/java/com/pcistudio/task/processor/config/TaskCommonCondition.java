package com.pcistudio.task.processor.config;


import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class TaskCommonCondition extends AnyNestedCondition {
    TaskCommonCondition() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(prefix = "spring.task.processor.writer", name = "enabled", havingValue = "true")
    static class WriterEnabled {

    }

    @ConditionalOnProperty(prefix = "spring.task.handlers", name = "enabled", havingValue = "true")
    static class HandlerEnabled {

    }

}
