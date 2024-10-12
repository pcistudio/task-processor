package com.pcistudio.task.procesor.register;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.Set;
import java.util.stream.Collectors;


class MysqlTaskStorageSetupTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {

        jdbcTemplate = new JdbcTemplate(
                new EmbeddedDatabaseBuilder()
                        .setType(EmbeddedDatabaseType.H2)
                        .build()
        );
    }

    @Test
    void createStorage() {
        MysqlTaskStorageSetup mysqlTaskStorageSetup = new MysqlTaskStorageSetup(jdbcTemplate);
        mysqlTaskStorageSetup.createStorage("test_table");
        jdbcTemplate.queryForList("SHOW TABLES").forEach(System.out::println);
        Set<String> tables = jdbcTemplate.queryForList("SHOW TABLES")
                .stream()
                .map(map -> map.get("TABLE_NAME").toString().toLowerCase())

                .collect(Collectors.toSet());
        org.assertj.core.api.Assertions.assertThat(tables).contains("test_table", "test_table_error");
    }
}