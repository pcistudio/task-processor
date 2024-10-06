// src/main/java/com/contact/manager/service/MailServiceImpl.java
package com.contact.manager.services;

import com.contact.manager.entities.Person;
import com.contact.manager.events.PersonEmailSentEvent;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;
    private final String company;
    private final String fromEmail;
    private final ApplicationEventPublisher eventPublisher;

    private final Set<String> blackListedDomains = Set.of("example.com", "example.org");

    public MailServiceImpl(JavaMailSender emailSender,
                           TemplateEngine templateEngine,
                           @Value("${company.name}") String company,
                           @Value("${spring.mail.username}") String fromEmail,
                           ApplicationEventPublisher eventPublisher) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
        this.company = company;
        this.fromEmail = fromEmail;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void sendEmail(String to, String subject, String text) {
        Person.Anonymous anonymous = Person.Anonymous.of(to);
        sendEmail(anonymous, subject, text);
    }

    @Override
    public void sendEmail(Person to, String subject, String text) {

        if (to.getEmail() == null || to.getEmail().isBlank()) {
            log.warn("Person email is empty, skipping email notification");
            return;
        }

        if (blackListedDomains.contains(to.getEmail().split("@")[1])) {
            log.warn("Email to {} is blacklisted by domain, not sending", to);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(String.format("%s <%s>", company, fromEmail));
        message.setTo(to.getEmail());
        message.setSubject(subject);
        message.setText(text);

        boolean status = true;
        try {
            emailSender.send(message);
            log.info("Sent text email to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email", e);
            status = false;
        }
        eventPublisher.publishEvent(new PersonEmailSentEvent(this, to, subject, text, status));
    }

    @Override
    public void sendEmailWithTemplate(String to, String subject, String templateName, Map<String, Object> templateParams) {
        sendEmailWithTemplate(Person.Anonymous.of(to), subject, templateName, templateParams);
    }

    @Override
    public void sendEmailWithTemplate(Person person, String subject, String templateName, Map<String, Object> templateParams) {

        if (person.getEmail() == null || person.getEmail().isBlank()) {
            log.warn("Person email for template is empty, skipping email notification");
            return;
        }

        if (blackListedDomains.contains(person.getEmail().split("@")[1])) {
            log.warn("Email with template to {} is blacklisted by domain, not sending", person.getEmail());
            return;
        }

        log.debug("Sending person email with template to to={}, subject={}, template={}, params={}", person.getEmail(), subject, templateName, templateParams);
        String emailText = createHtmlBody(templateName, templateParams);
        sendMimeEmail(person, subject, emailText, extractAttachments(templateParams));

    }

    private Map<String, Object> extractAttachments(Map<String, Object> templateParams) {
        return templateParams.entrySet().stream()
                .filter(entry -> canBeAttached(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean canBeAttached(Object value) {
        return value instanceof File || value instanceof InputStream;
    }


    private String createHtmlBody(String templateName, Map<String, Object> templateParams) {
        Context context = new Context();
        context.setVariables(templateParams);
        return templateEngine.process(templateName, context);
    }

    private void sendMimeEmail(Person person, String subject, String htmlBody, Map<String, Object> attachments) {

        MimeMessage message = emailSender.createMimeMessage();
        boolean status = true;
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(String.format("%s <%s>", company, fromEmail));
            helper.setTo(person.getEmail());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (attachments.isEmpty()) {
                log.debug("No attachments person add person email");
            } else {
                addAttachments(person.getEmail(), attachments, helper);
            }

            emailSender.send(message);
            log.info("Sent html email person {}", person);

        } catch (MessagingException e) {
            status = false;
            log.error("Failed person send email", e);
        }
        eventPublisher.publishEvent(new PersonEmailSentEvent(this, person, subject, htmlBody, status));
    }

    private void addAttachments(String to, Map<String, Object> attachments, MimeMessageHelper helper) {
        attachments.forEach((name, attachment) -> {
            try {

                if (attachment instanceof File fileAttachment) {
                    log.debug("Adding attachment {} to email={} using File", name, to);
                    log.debug("file size: {}", fileAttachment.length());
                    helper.addAttachment(name, new FileDataSource(fileAttachment));
                } else if (attachment instanceof InputStream inputStreamAttachment) {
                    log.debug("Adding attachment {} to email={} using InputStream", name, to);
                    helper.addAttachment(name, () -> inputStreamAttachment);
                } else {
                    throw new IllegalArgumentException("Unsupported attachment type");
                }
            } catch (MessagingException e) {
                log.error("Failed to add attachment to email={}", to, e);
            }
        });
        log.info("Added {} attachments to email={}", attachments.size(), to);
    }

}