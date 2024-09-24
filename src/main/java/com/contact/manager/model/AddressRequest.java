// src/main/java/com/contact/manager/model/AddressRequest.java
package com.contact.manager.model;

import com.contact.manager.entities.contraints.AtLeastOneField;

@AtLeastOneField(fields = {"street", "city", "state", "zipCode", "country"}, message = "At least one of the fields=[street, city, state, zipCode, country] must be present")
public class AddressRequest {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}