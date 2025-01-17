package com.pcistudio.task.processor.config;

import com.pcistudio.task.procesor.register.H2TaskStorageSetup;
import com.pcistudio.task.procesor.register.MariadbTaskStorageSetup;
import com.pcistudio.task.procesor.register.MysqlTaskStorageSetup;
import com.pcistudio.task.procesor.register.TaskStorageSetup;
import com.pcistudio.task.procesor.template.LoggingJdbcTemplate;
import com.pcistudio.task.processor.util.TaskProcessorDataSourceResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
//@Configuration(proxyBeanMethods = false)
@Configuration
@Conditional(TaskCommonCondition.class)
@Import(DecodingConfiguration.class)
public class TaskProcessorJdbcTemplateAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Configuration
    @ConditionalOnProperty(prefix = "spring.task.logging", name = "template", havingValue = "false", matchIfMissing = true)
    static class DefaultJdbcTemplate {
        @Bean
        @ConditionalOnMissingBean(name = "taskProcessorJdbcTemplate")
        JdbcTemplate taskProcessorJdbcTemplate(TaskProcessorDataSourceResolver dataSourceResolver) {
            DataSource dataSource = dataSourceResolver.resolveDatasource();
            log.info("TaskProcessor using dataSource: {}", dataSource.getClass().getName());
            return new JdbcTemplate(dataSource);
        }
    }

    @ConditionalOnProperty(prefix = "spring.task.logging", name = "template", havingValue = "true")
    static class LoggingTemplate {
        @Bean
        @ConditionalOnMissingBean(name = "taskProcessorJdbcTemplate")
        JdbcTemplate taskProcessorJdbcTemplate(TaskProcessorDataSourceResolver dataSourceResolver) {
            DataSource dataSource = dataSourceResolver.resolveDatasource();
            log.info("TaskProcessor using dataSource: {}", dataSource.getClass().getName());
            return new LoggingJdbcTemplate(dataSource);
        }
    }

    @Bean
    TaskProcessorDataSourceResolver taskProcessorDataSourceResolver() {
        return new TaskProcessorDataSourceResolver(applicationContext);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @AutoConfiguration
    @ConditionalOnMissingBean(TaskStorageSetup.class)
    @ConditionalOnClass(name = {"org.h2.Driver", "com.pcistudio.task.procesor.writer.H2TaskInfoWriter"})
    static class H2 {
        private final JdbcTemplate jdbcTemplate;

        public H2(@Qualifier("taskProcessorJdbcTemplate") JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Bean
        TaskStorageSetup taskStorageSetup() {
            return new H2TaskStorageSetup(jdbcTemplate);
        }
    }

    @AutoConfiguration
    @ConditionalOnMissingBean(TaskStorageSetup.class)
    @ConditionalOnClass(name = {"org.mariadb.jdbc.Driver", "com.pcistudio.task.procesor.writer.MariadbTaskInfoWriter"})
    static class Mariadb {
        private final JdbcTemplate jdbcTemplate;

        public Mariadb(@Qualifier("taskProcessorJdbcTemplate") JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Bean
        TaskStorageSetup taskStorageSetup() {
            return new MariadbTaskStorageSetup(jdbcTemplate);
        }
    }

    @AutoConfiguration
    @ConditionalOnMissingBean(TaskStorageSetup.class)
    @ConditionalOnClass(name = {"com.mysql.cj.jdbc.Driver", "com.pcistudio.task.procesor.writer.MysqlTaskInfoWriter"})
    static class Mysql {
        private final JdbcTemplate jdbcTemplate;

        public Mysql(@Qualifier("taskProcessorJdbcTemplate") JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Bean
        TaskStorageSetup mysqlTaskStorageSetup() {
            return new MysqlTaskStorageSetup(jdbcTemplate);
        }
    }
}