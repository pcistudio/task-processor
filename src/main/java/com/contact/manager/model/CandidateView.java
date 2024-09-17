package com.contact.manager.model;

import com.contact.manager.entities.Candidate;
import org.springframework.util.Assert;

public class CandidateView {
    private Long id;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String email;
    private String phone;

    public CandidateView(Candidate candidate) {
        Assert.notNull(candidate, "Candidate must not be null");
        this.id = candidate.getId();
        this.lastName = candidate.getLastName();
        this.firstName = candidate.getFirstName();
        this.jobTitle = candidate.getJobTitle();
        this.email = candidate.getEmail();
        this.phone = candidate.getMobile() != null ? candidate.getMobile() : candidate.getOfficePhone();
    }
    public Long getId() {
        return id;
    }

    public CandidateView setId(Long id) {
        this.id = id;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public CandidateView setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public CandidateView setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public CandidateView setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public CandidateView setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public CandidateView setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
}
