// src/main/java/com/contact/manager/model/ContactRequest.java
package com.contact.manager.model;

public class ContactRequest {
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String officePhone;
    private String mobile;
    private String description;
    private AddressRequest primaryAddress;
    private AddressRequest secondaryAddress;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AddressRequest getPrimaryAddress() {
        return primaryAddress;
    }

    public void setPrimaryAddress(AddressRequest primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    public AddressRequest getSecondaryAddress() {
        return secondaryAddress;
    }

    public void setSecondaryAddress(AddressRequest secondaryAddress) {
        this.secondaryAddress = secondaryAddress;
    }
}