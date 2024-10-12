package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.register.DefaultStorageResolver;
import com.pcistudio.task.procesor.register.ProcessorRegisterImpl;
import com.pcistudio.task.procesor.register.TaskStorageSetup;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;


@RequiredArgsConstructor
public abstract class TaskWriterConfiguration {

    @Bean
    ProcessorRegisterImpl processorRegister(TaskStorageSetup taskStorageSetup) {
        ProcessorRegisterImpl.Builder builder = new ProcessorRegisterImpl.Builder();
        builder.taskTableSetup(taskStorageSetup);
        addTask(builder);
        return builder.build();
    }

    @Bean
    StorageResolver storageResolver(ProcessorRegisterImpl processorRegister) {

        Assert.notNull(processorRegister, "processorRegister is required");
        return new DefaultStorageResolver(processorRegister);
    }

    protected abstract void addTask(ProcessorRegisterImpl.Builder builder);
}
