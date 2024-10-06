package com.contact.manager.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Override
    @Transactional()
    Notification save(Notification entity);

}