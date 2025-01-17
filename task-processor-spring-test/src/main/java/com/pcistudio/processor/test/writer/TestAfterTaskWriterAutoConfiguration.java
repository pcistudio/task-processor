package com.pcistudio.processor.test.writer;

import com.pcistudio.task.procesor.writer.TaskWriter;
import com.pcistudio.task.processor.config.TaskWriterAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = TaskWriterAutoConfiguration.class)
public class TestAfterTaskWriterAutoConfiguration {
    @Bean
    TaskWriterHelper taskWriterHelper(TaskWriter taskWriter) {
        return new TaskWriterHelper(taskWriter);
    }
}