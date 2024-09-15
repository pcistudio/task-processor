package com.contact.manager.model;

import com.contact.manager.entities.Contact;
import org.springframework.util.Assert;

public class ContactView {
    private Long id;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String email;
    private String phone;

    public ContactView(Contact contact) {
        Assert.notNull(contact, "Contact must not be null");
        this.id = contact.getId();
        this.lastName = contact.getLastName();
        this.firstName = contact.getFirstName();
        this.jobTitle = contact.getJobTitle();
        this.email = contact.getEmail();
        this.phone = contact.getMobile() != null ? contact.getMobile() : contact.getOfficePhone();
    }
    public Long getId() {
        return id;
    }

    public ContactView setId(Long id) {
        this.id = id;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public ContactView setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public ContactView setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ContactView setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public ContactView setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public ContactView setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
}
