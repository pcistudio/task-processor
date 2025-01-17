package com.pcistudio.task.processor.task;

import com.pcistudio.task.procesor.handler.TaskInfoVisibilityService;
import com.pcistudio.task.procesor.task.TaskParams;
import com.pcistudio.task.procesor.writer.TaskWriter;
import com.pcistudio.task.processor.config.TaskProcessorConfig;
import com.pcistudio.task.processor.model.EncodeVideoCommand;
import com.pcistudio.task.processor.model.Person;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class WriterScheduler {
    private final TaskWriter taskWriter;
    private final TaskInfoVisibilityService taskInfoVisibilityService;
    private final SecureRandom random = new SecureRandom();

    @Scheduled(cron = "0 0/1 * * * ?")
    public void writePersons() {
        int count = 10_000;
        generateTasks(TaskProcessorConfig.PERSON_HANDLER, count, this::generatePerson);

    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void writeVideo() {
        int count = 10_000;
        generateTasks(TaskProcessorConfig.VIDEO_HANDLER, count, this::generateVideo);
    }

    private void generateTasks(String handlerName, int count, Supplier<TaskParams> supplier) {
        if (taskInfoVisibilityService.allTaskCompleted(handlerName, LocalDate.now())) {
            log.info("Writing {} persons to process", count);
            for (int i = 0; i < count; i++) {
                taskWriter.writeTasks(
                        supplier.get()
                );
            }
            log.info("Finish writing {} persons to process", count);
        } else {
            log.info("Still processing previews tasks");
        }
    }

    private TaskParams generatePerson() {
        int age = random.nextInt(80);
        return TaskParams.builder()
                .handlerName(TaskProcessorConfig.PERSON_HANDLER)
                .payload(new Person("Name LastName" + age, age))
                .build();
    }

    private TaskParams generateVideo() {
        int id = random.nextInt(2000_000);
        return TaskParams.builder()
                .handlerName(TaskProcessorConfig.VIDEO_HANDLER)
                .payload(
                        EncodeVideoCommand.builder()
                                .videoId(UUID.randomUUID())
                                .videoPath("/path/video" + id)
                                .build()
                )
                .build();
    }
}
