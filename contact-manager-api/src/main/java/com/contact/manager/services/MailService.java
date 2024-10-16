// src/main/java/com/contact/manager/service/MailService.java
package com.contact.manager.services;

import com.contact.manager.entities.Person;
import com.contact.manager.util.TemplateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface MailService {
    Logger log = LoggerFactory.getLogger(MailService.class);

    void sendEmail(String to, String subject, String text);

    void sendEmail(Person to, String subject, String text);

    void sendEmailWithTemplate(String to, String subject, String templateName, Map<String, Object> templateParams);

    void sendEmailWithTemplate(Person person, String subject, String templateName, Map<String, Object> templateParams);

    default void sendEmailWithTemplate(String to, String subject, String templateName, Supplier<Map<String, Object>> templateParamsSupplier) {
        sendEmailWithTemplate(to, subject, templateName, templateParamsSupplier.get());
    }

    default void sendEmailWithTemplate(String to, String subject, String templateName, Object targetObject) {
        sendEmailWithTemplate(to, subject, templateName, () -> TemplateHelper.getObjectFieldsAndValues(targetObject));
    }

    default void sendEmailWithTemplate(Person person, String subject, String templateName, Supplier<Map<String, Object>> templateParamsSupplier) {
        sendEmailWithTemplate(person, subject, templateName, templateParamsSupplier.get());
    }

    default void sendEmailWithTemplate(Person person, String subject, String templateName) {
        sendEmailWithTemplate(person, subject, templateName, () -> TemplateHelper.getObjectFieldsAndValues(person));
    }

    default void sendEmailsToPersons(List<? extends Person> persons, String subject, String templateName) {
        if (persons == null || persons.isEmpty()) {
            log.warn("No persons to send emails to");
            return;
        }
        persons.forEach(
                person -> {
                    try {
                        sendEmailWithTemplate(person, subject, templateName);
                    } catch (Exception e) {
                        log.error("Error sending email to {}", person.getEmail(), e);
                    }
                }
        );
    }


}