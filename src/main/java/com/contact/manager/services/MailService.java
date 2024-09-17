// src/main/java/com/contact/manager/service/MailService.java
package com.contact.manager.services;

import java.util.Map;

public interface MailService {
    void sendEmail(String to, String subject, String text);
    void sendEmailWithTemplate(String to, String subject, String templateName, Map<String, Object> templateModel);
}