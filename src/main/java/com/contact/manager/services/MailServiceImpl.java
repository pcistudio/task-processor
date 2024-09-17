// src/main/java/com/contact/manager/service/MailServiceImpl.java
package com.contact.manager.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Set;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;
    private final String company;
    private final String fromEmail;

    private final Set<String> blackListedDomains = Set.of("example.com", "example.org");

    public MailServiceImpl(JavaMailSender emailSender,
                           TemplateEngine templateEngine,
                           @Value("${company.name}") String company,
                           @Value("${spring.mail.username}") String fromEmail) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
        this.company = company;
        this.fromEmail = fromEmail;
    }
    @Override
    public void sendEmail(String to, String subject, String text) {
        if (blackListedDomains.contains(to.split("@")[1])) {
            log.warn("Email to {} is blacklisted by domain, not sending", to);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(String.format("%s <%s>", company, fromEmail));
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
        log.info("Sent text email to {}", to);
    }

    @Override
    public void sendEmailWithTemplate(String to, String subject, String templateName, Map<String, Object> templateModel) {
        if (blackListedDomains.contains(to.split("@")[1])) {
            log.warn("Email with template to {} is blacklisted by domain, not sending", to);
            return;
        }

        Context context = new Context();
        context.setVariables(templateModel);
        String htmlBody = templateEngine.process(templateName, context);

        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(String.format("%s <%s>", company, fromEmail));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            emailSender.send(message);
            log.info("Sent html email to {}", to);
        } catch (MessagingException e) {
            throw new IllegalArgumentException("Failed to send email", e);
        }
    }
}