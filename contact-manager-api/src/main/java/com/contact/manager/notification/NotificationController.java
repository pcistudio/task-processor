package com.contact.manager.notification;

import com.contact.manager.model.batch.BatchInfoDefault;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationServiceImpl;

    @PostMapping("batch:send")
    public ResponseEntity<BatchInfoDefault> sendNotificationToQueue(@RequestBody NotificationRequest notificationRequest) {
        List<Notification> notification = toNotificationList(notificationRequest.getNotifications());
        return ResponseEntity.ok(notificationServiceImpl.sendNotification(notification));
    }

    @PostMapping("single:send")
    public ResponseEntity<Notification> sendNotificationToQueue(@RequestBody NotificationRequest.NotificationModel notification) {
        return ResponseEntity.ok(notificationServiceImpl.sendNotification(toNotification(notification)));
    }


    @GetMapping("/templates")
    public ResponseEntity<List<String>> getTemplates() {
        return ResponseEntity.ok(notificationServiceImpl.geMessageTemplates());
    }

    private List<Notification> toNotificationList(List<NotificationRequest.NotificationModel> notification) {
        return notification.stream()
                .map(NotificationController::toNotification)
                .toList();
    }

    private static Notification toNotification(NotificationRequest.NotificationModel n) {
        return new Notification()
                .setEmail(n.getEmail())
                .setFirstName(n.getFirstName())
                .setLastName(n.getLastName())
                .setMessage(n.getMessage())
                .setSubject(n.getSubject())
                .setPersonType(n.getPersonType())
                .setTemplateName(n.getTemplateName());
    }

}
