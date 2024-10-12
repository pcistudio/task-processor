package com.pcistudio.task.procesor.writer;

import com.pcistudio.task.procesor.StorageResolver;
import com.pcistudio.task.procesor.register.MysqlTaskStorageSetup;
import com.pcistudio.task.procesor.util.JsonUtil;
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

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@ContextConfiguration(classes = MysqlTaskWriterTest.JdbcTemplateConfig.class)
class MysqlTaskWriterTest {

    @Configuration
    public static class JdbcTemplateConfig {
        @Bean
        JdbcTemplate jdbcTemplate()  {
            EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("testdb;MODE=MYSQL;DATABASE_TO_LOWER=TRUE")
                    .build();
            return new JdbcTemplate(db);
        }

        @Bean
        MysqlTaskStorageSetup mysqlTaskStorageSetup(JdbcTemplate jdbcTemplate) {
            MysqlTaskStorageSetup taskStorageSetup = new MysqlTaskStorageSetup(jdbcTemplate);
            taskStorageSetup.createStorage("test_table");
            return taskStorageSetup;
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void writeTasks() {
        TaskWriter mysqlTaskWriter = new MysqlTaskWriter(jdbcTemplate, StorageResolver.IDENTITY);

        mysqlTaskWriter.writeTasks("test_table", new Person("Test Name", 31));
        jdbcTemplate.queryForObject("SELECT * FROM test_table", (rs, rowNum) -> {
            Person person = JsonUtil.from(rs.getString("payload"), Person.class);
            assertEquals("Test Name", person.name());
            assertEquals(31, person.age());
            return null;
        });

    }

    record Person(String name, int age) {
    }


}