package com.contact.manager.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class NotificationRequest {
    private List<NotificationModel> notifications;

    public List<NotificationModel> getNotifications() {
        return notifications;
    }

    public NotificationRequest setNotifications(List<NotificationModel> notifications) {
        this.notifications = notifications;
        return this;
    }

    public static class NotificationModel {
        @Size(min = 1, max = 250)
        private String subject;
        @Size(min = 1, max = 50)
        private String templateName;
        @NotNull
        private PersonType personType;
        @Size(min = 1, max = 1024)
        private String message;
        @NotBlank
        @Size(min = 1, max = 70)
        private String email;
        @NotBlank
        @Size(min = 1, max = 50)
        private String firstName;
        @Size(min = 1, max = 50)
        private String lastName;

        public String getSubject() {
            return subject;
        }

        public PersonType getPersonType() {
            return personType;
        }

        public NotificationModel setPersonType(PersonType personType) {
            this.personType = personType;
            return this;
        }

        public NotificationModel setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public String getTemplateName() {
            return templateName;
        }

        public NotificationModel setTemplateName(String templateName) {
            this.templateName = templateName;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public NotificationModel setMessage(String message) {
            this.message = message;
            return this;
        }

        public String getEmail() {
            return email;
        }

        public NotificationModel setEmail(String email) {
            this.email = email;
            return this;
        }

        public String getFirstName() {
            return firstName;
        }

        public NotificationModel setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public String getLastName() {
            return lastName;
        }

        public NotificationModel setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }


    }
}
