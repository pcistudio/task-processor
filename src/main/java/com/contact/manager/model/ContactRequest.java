// src/main/java/com/contact/manager/model/ContactRequest.java
package com.contact.manager.model;

import com.contact.manager.entities.contraints.AtLeastOneField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@AtLeastOneField(fields = {"officePhone", "mobile", "email"}, message = "At least one of the fields=[officePhone, mobile, email] must be present")
public class ContactRequest {
    @NotBlank
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String officePhone;
    private String mobile;
    private String description;
    @Valid
    private AddressRequest primaryAddress;
    @Valid
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