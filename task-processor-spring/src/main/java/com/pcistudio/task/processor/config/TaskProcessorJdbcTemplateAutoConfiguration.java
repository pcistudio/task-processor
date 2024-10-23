package com.pcistudio.task.processor.config;

import com.pcistudio.task.processor.util.TaskProcessorDataSourceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Slf4j
//@Configuration(proxyBeanMethods = false)
@Configuration
public class TaskProcessorJdbcTemplateAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean(name = "taskProcessorJdbcTemplate")
    JdbcTemplate taskProcessorJdbcTemplate() {
        TaskProcessorDataSourceHelper dataSourceHelper = new TaskProcessorDataSourceHelper(applicationContext);
        DataSource dataSource = dataSourceHelper.resolveDatasource();
        log.info("TaskProcessor using dataSource: {}", dataSource.getClass().getName());
        return new JdbcTemplate(dataSource);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}