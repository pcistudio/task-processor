package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.handler.TaskProcessorManager;

public interface TaskProcessorManagerCustomizer {
    void customize(TaskProcessorManager taskProcessorManager);
}
