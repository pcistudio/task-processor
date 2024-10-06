package com.contact.manager.notification.processing;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class NotificationScheduler {

    @Scheduled(cron = "0 0/1 * * * ?")
    @SchedulerLock(name = "sendingNotifications",
            lockAtLeastFor = "PT1M", lockAtMostFor = "PT5M")
    public void sendNotifications() {
        log.info("Sending notifications");
    }
}
