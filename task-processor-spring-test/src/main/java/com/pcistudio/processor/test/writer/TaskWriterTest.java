package com.pcistudio.processor.test.writer;

import com.pcistudio.processor.test.config.AutoConfigureTaskProcessorJdbcTemplate;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@JdbcTest
@AutoConfigureTestTaskWriter
@AutoConfigureTaskProcessorJdbcTemplate
public @interface TaskWriterTest {
}
