package com.pcistudio.task.procesor.template;

import com.pcistudio.task.procesor.util.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

@Slf4j
public class LoggingJdbcTemplate extends JdbcTemplate {
    private final boolean showSql;

    public LoggingJdbcTemplate(DataSource dataSource, boolean showSql) {
        super(dataSource);
        this.showSql = showSql;
    }

    public LoggingJdbcTemplate(DataSource dataSource) {
        this(dataSource, false);
    }

    @Override
    public int update(String sql, @Nullable PreparedStatementSetter pss) {
        int update = super.update(sql, pss);
        logSqlAndParameters(sql, pss);
        return update;
    }

    @Override
    public <T> List<T> query(String sql, @Nullable PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        List<T> query = super.query(sql, pss, rowMapper);
        logSqlAndParameters(sql, pss);
        return query;
    }

    @Override
    @Nullable
    public <T> T query(String sql, @Nullable PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException {
        T query = super.query(sql, pss, rse);
        logSqlAndParameters(sql, pss);
        return query;
    }

    @SuppressWarnings("PMD.GuardLogStatement")
    private void logSqlAndParameters(String sql, @Nullable PreparedStatementSetter pss) {
        if (!log.isDebugEnabled()) {
            return;
        }

        if (showSql) {
            log.debug("SQL: " + sql);
        }

        if (pss instanceof ArgumentPreparedStatementSetter) {

            Field argsFields = ReflectionUtils.findField(ArgumentPreparedStatementSetter.class, "args");
            if (argsFields == null) {
                log.debug("No parameters");

                return;
            }
            ReflectionUtils.makeAccessible(argsFields);
            Object[] args = (Object[]) ReflectionUtils.getField(argsFields, pss);
            if (args == null) {
                log.debug("No parameters");
                return;
            }

            log.debug("Parameters: ");
            for (Object arg : args) {
                Assert.notNull(arg, "Argument must not be null");
                if (arg instanceof Instant instant) {
                    log.debug("millis: {}", instant.toEpochMilli());
                } else {
                    log.debug(arg.toString());
                }
            }
        }
        log.debug("---------------------------------------------");
    }
}