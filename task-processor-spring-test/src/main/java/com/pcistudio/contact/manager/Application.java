package com.pcistudio.contact.manager;

import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.procesor.task.TaskParams;
import com.pcistudio.task.procesor.writer.TaskWriter;
import com.pcistudio.task.processor.config.AbstractHandlersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class Application implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    TaskService taskService;

    @Override
    public void run(String... args) throws Exception {
        taskService.writeTask("Hello World");
    }

    @Configuration
    static class TestConfig {
//        @Bean
//        DataSource dataSource() {
//            EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
//                    .setType(EmbeddedDatabaseType.H2)
//                    .setName("testdb;MODE=MYSQL;DATABASE_TO_LOWER=TRUE")
//                    .build();
////            return new JdbcTemplate(db);
//            return db;
//        }

        @Bean
        JdbcTemplate taskProcessorJdbcTemplate() {
            EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("testdb;MODE=MYSQL;DATABASE_TO_LOWER=TRUE")
                    .build();
            return new JdbcTemplate(db);
//            return db;
        }
    }

    @Configuration
    static class TestHandlerConfiguration extends AbstractHandlersConfiguration {
        @Override
        protected void addTask(HandlerManagerImpl.Builder builder) {
            builder.register(HandlerProperties.builder()
                    .handlerName("test")
                    .tableName("test")
                    .taskHandler(payload -> {
                        System.out.println("test");
                    })
                    .taskHandlerType(String.class)
                    .build());
        }
    }

    @Service
    static class TaskService {
        private final TaskWriter taskWriter;

        public TaskService(TaskWriter taskWriter) {
            this.taskWriter = taskWriter;
        }

        public void writeTask(String payload) {
            taskWriter.writeTasks(TaskParams.builder()
                    .handlerName("test")
                    .payload(payload)
                    .build());
        }
    }
}