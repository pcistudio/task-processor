package com.pcistudio.processor.test.handler;

import com.pcistudio.processor.test.config.AutoConfigureTaskProcessorJdbcTemplate;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@JdbcTest
@AutoConfigureTaskProcessorJdbcTemplate
@AutoConfigureTestTaskProcessorManager
public @interface TaskProcessorTest {
}
