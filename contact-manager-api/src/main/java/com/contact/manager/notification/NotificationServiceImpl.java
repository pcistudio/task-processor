package com.contact.manager.notification;

import com.contact.manager.model.batch.BatchInfoDefault;
import com.pcistudio.task.procesor.task.TaskMetadata;
import com.pcistudio.task.procesor.task.TaskParams;
import com.pcistudio.task.procesor.writer.TaskWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
//@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final ThymeleafTemplateInfo thymeleafTemplateInfo;
    private final TaskWriter taskWriter;

    public NotificationServiceImpl(NotificationRepository notificationRepository, ThymeleafTemplateInfo thymeleafTemplateInfo, @Qualifier("taskWriter") TaskWriter taskInfoWriter) {
        this.notificationRepository = notificationRepository;
        this.thymeleafTemplateInfo = thymeleafTemplateInfo;
        this.taskWriter = taskInfoWriter;
    }

    @Override
    @Transactional
    public BatchInfoDefault sendNotification(List<Notification> notifications) {

        BatchInfoDefault batchInfo = new BatchInfoDefault();
        for (Notification notification : notifications) {
            notification.setBatchId(batchInfo.getBatchId());
            notification.setStatus(ProcessStatus.PENDING);
        }

        batchInfo.addSuccessful(notificationRepository.saveAll(notifications));
        log.info("Notification batch {} saved", batchInfo.getBatchId());
        return batchInfo;
    }

    @Transactional
    @Override
    public Notification sendNotification(Notification notification) {
        UUID batchId = UUID.randomUUID();

        notification.setBatchId(batchId);

        Notification save = notificationRepository.save(notification);
        TaskMetadata taskInfo = taskWriter.writeTasks(
                TaskParams.builder()
                        .handlerName("email")
                        .payload(notification)
                        .build()
        );
        log.info("{}", taskInfo);
        log.info("Notification batch {} saved", batchId);
        return save;
    }

    @Override

    public List<String> geMessageTemplates() {
        return thymeleafTemplateInfo.get();
    }

    @Component
    @EnableConfigurationProperties({ThymeleafProperties.class})
    static class ThymeleafTemplateInfo implements Supplier<List<String>> {
        private Resource resource;
        private String suffix;

        public ThymeleafTemplateInfo(ThymeleafProperties properties, ResourceLoader resourceLoader) {
            this.resource = resourceLoader.getResource(properties.getPrefix());
            this.suffix = properties.getSuffix();
        }

        @Cacheable(value = "templates")
        public List<String> get() {
            try {
                File[] files = resource.getFile().listFiles((dir, name) -> name.endsWith(suffix));
                Assert.notNull(files, "Error looking for Thymeleaf templates at " + resource.getURL());
                List<String> list = Arrays.stream(files)
                        .map(File::getName)
                        .map(name -> name.substring(0, name.length() - suffix.length()))
                        .toList();

                log.info("Found {} Thymeleaf templates", list.size());
                return list;
            } catch (IOException e) {
                throw new IllegalStateException("Error looking for Thymeleaf templates at ", e);
            }
        }
    }

}
