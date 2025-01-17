package com.pcistudio.task.processor.config;

import com.pcistudio.processor.test.handler.RandomTaskHandler;
import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.handler.TaskHandler;
import com.pcistudio.task.procesor.handler.TaskHandlerTransientException;
import com.pcistudio.task.procesor.handler.TaskTransientException;
import com.pcistudio.task.procesor.register.HandlerManagerImpl;
import com.pcistudio.task.procesor.util.JsonUtil;
import com.pcistudio.task.processor.model.EncodeVideoCommand;
import com.pcistudio.task.processor.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Configuration
public class TaskProcessorConfig extends AbstractHandlersConfiguration {
    public static final String PERSON_HANDLER = "person";
    public static final String VIDEO_HANDLER = "mp4_video_encoder";

    @Override
    protected void addTask(HandlerManagerImpl.Builder builder) {
        builder.register(
                HandlerProperties.builder()
                        .handlerName(PERSON_HANDLER)
                        .tableName(PERSON_HANDLER)
                        .requeueInterval(120000)
                        .processingExpire(Duration.ofMinutes(2))
                        .transientExceptions(Set.of(TransientDataAccessException.class))
                        .taskHandler(new PrintPersonHandler())
                        .build()
        ).register(
                HandlerProperties.builder()
                        .handlerName(VIDEO_HANDLER)
                        .tableName(VIDEO_HANDLER)
                        .requeueInterval(120000)
                        .processingExpire(Duration.ofMinutes(2))
                        .transientExceptions(Set.of(TransientDataAccessException.class))
                        .taskHandler(getVideoHandler())
                        .taskHandlerType(EncodeVideoCommand.class)
                        .build()
        );
    }

    static class PrintPersonHandler implements TaskHandler<Person> {
        @Override
        public void process(Person payload) {
            JsonUtil.print("Person ", payload);
        }
    }

    TaskHandler<EncodeVideoCommand> getVideoHandler() {
        return new RandomTaskHandler<EncodeVideoCommand>().builder()
                .withConsumer(video -> log.info("Encoding videoId={}", video.getVideoId()))
                .withTaskCount(50)
                .withExpectedException(new RuntimeException("Mock Runtime Exception"), 1)
                .withExpectedException(new TaskTransientException("Mock error saving in DB"), 3)
                .withExpectedException(new TaskHandlerTransientException("Mock error saving in handler"), 3)
                .withExpectedException(new IllegalArgumentException("Mock Illegal Argument"), 3)
                .withSlowTaskCount(8)
                .withSlowTaskDurationMs(2001)
                .enableRandomizeDurationCalls()
                .withStopErrorsCallsAfter(null)
                .withStopSlowCallsAfter(null)
                .build();
    }

}
