package com.contact.manager.notification;

import com.contact.manager.model.batch.BatchInfoDefault;

import java.util.List;

public interface NotificationService {

    BatchInfoDefault sendNotification(List<Notification> notification);

    Notification sendNotification(Notification notification);

    List<String> geMessageTemplates();
}
