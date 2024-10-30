package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.register.H2TaskStorageSetup;
import com.pcistudio.task.procesor.register.MariadbTaskStorageSetup;
import com.pcistudio.task.procesor.register.MysqlTaskStorageSetup;
import com.pcistudio.task.procesor.register.TaskStorageSetup;
import com.pcistudio.task.procesor.util.encoder.MessageEncoding;
import com.pcistudio.task.procesor.writer.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;

@Configuration
@ConditionalOnProperty(prefix = "spring.task.processor.writer", name = "enabled", havingValue = "true")
@ConditionalOnBean(name = "taskProcessorJdbcTemplate")
public class TaskWriterAutoConfiguration {
//  Writer has to be enabled
    @AutoConfiguration
    @ConditionalOnMissingBean(TaskInfoWriter.class)
    @ConditionalOnClass(name = {"org.h2.Driver", "com.pcistudio.task.procesor.writer.H2TaskInfoWriter" })
    static class H2 {
        private final JdbcTemplate jdbcTemplate;
        public H2(@Qualifier("taskProcessorJdbcTemplate") JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Bean
        TaskInfoWriter taskInfoWriter(StorageResolver storageResolver) {
            return new H2TaskInfoWriter(jdbcTemplate, storageResolver);
        }

//        @Bean
//        TaskStorageSetup taskStorageSetup() {
//            return new H2TaskStorageSetup(jdbcTemplate);
//        }
    }

    @AutoConfiguration
    @ConditionalOnMissingBean(TaskInfoWriter.class)
    @ConditionalOnClass(name = {"org.mariadb.jdbc.Driver", "com.pcistudio.task.procesor.writer.MariadbTaskInfoWriter" })
    static class Mariadb {
        private final JdbcTemplate jdbcTemplate;
        public Mariadb(@Qualifier("taskProcessorJdbcTemplate") JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Bean
        TaskInfoWriter taskInfoWriter(StorageResolver storageResolver) {
            return new MariadbTaskInfoWriter(jdbcTemplate, storageResolver);
        }

//        @Bean
//        TaskStorageSetup taskStorageSetup() {
//            return new MariadbTaskStorageSetup(jdbcTemplate);
//        }
    }

    @AutoConfiguration
    @ConditionalOnMissingBean(TaskInfoWriter.class)
    @ConditionalOnClass(name = {"com.mysql.cj.jdbc.Driver", "com.pcistudio.task.procesor.writer.MysqlTaskInfoWriter"})
    static class Mysql {
        private final JdbcTemplate jdbcTemplate;

        public Mysql(@Qualifier("taskProcessorJdbcTemplate") JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Bean
        TaskInfoWriter taskInfoWriter(StorageResolver storageResolver) {
            return new MysqlTaskInfoWriter(jdbcTemplate, storageResolver);
        }

//        @Bean
//        TaskStorageSetup mysqlTaskStorageSetup() {
//            return new MysqlTaskStorageSetup(jdbcTemplate);
//        }
    }

    @Bean
    TaskWriter taskWriter(TaskInfoWriter taskInfoWriter, MessageEncoding messageEncoding, Clock clock) {///this in the builder
        return new TaskWriter(taskInfoWriter, messageEncoding, clock);
    }
}
