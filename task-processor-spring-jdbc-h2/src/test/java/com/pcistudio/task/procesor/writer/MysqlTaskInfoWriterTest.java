package com.pcistudio.task.procesor.writer;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.register.H2TaskStorageSetup;
import com.pcistudio.task.procesor.task.TaskParams;
import com.pcistudio.task.procesor.util.JsonUtil;
import com.pcistudio.task.procesor.util.encoder.JsonMessageEncoding;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@ContextConfiguration(classes = MysqlTaskInfoWriterTest.JdbcTemplateConfig.class)
class MysqlTaskInfoWriterTest {

    @Configuration
    public static class JdbcTemplateConfig {
        @Bean
        JdbcTemplate jdbcTemplate() {
            EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("testdb;MODE=MYSQL;DATABASE_TO_LOWER=TRUE")
                    .build();
            return new JdbcTemplate(db);
        }

        @Bean
        H2TaskStorageSetup mysqlTaskStorageSetup(JdbcTemplate jdbcTemplate) {
            H2TaskStorageSetup taskStorageSetup = new H2TaskStorageSetup(jdbcTemplate);
            taskStorageSetup.createStorage("test_table");
            return taskStorageSetup;
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void writeTasks() {
        TaskInfoWriter mysqlTaskInfoWriter = new H2TaskInfoWriter(jdbcTemplate, StorageResolver.IDENTITY);
        TaskWriter taskWriter = new TaskWriter(mysqlTaskInfoWriter, new JsonMessageEncoding());
        taskWriter.writeTasks(
                TaskParams.builder()
                        .handlerName("test_table")
                        .payload(new Person("Test Name", 31))
                        .build()
        );
        Person person1 = jdbcTemplate.queryForObject("SELECT * FROM test_table", (rs, rowNum) -> {
            Person person = JsonUtil.from(rs.getString("payload"), Person.class);
            assertEquals("Test Name", person.name());
            assertEquals(31, person.age());
            return person;
        });

        assertNotNull(person1);

    }

    record Person(String name, int age) {
    }


}