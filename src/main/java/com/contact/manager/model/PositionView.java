// src/main/java/com/contact/manager/model/PositionView.java
package com.contact.manager.model;

import com.contact.manager.entities.Position;

import java.util.List;

public class PositionView {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String department;
    private int numberOfPositions;
    private int salary;
    private List<String> requirements;
    private List<String> responsibilities;

    private PositionView(Position position) {
        this.id = position.getId();
        this.title = position.getTitle();
        this.description = position.getDescription();
        this.location = position.getLocation();
        this.department = position.getDepartment();
        this.numberOfPositions = position.getNumberOfPositions();
        this.salary = position.getSalary();
        this.requirements = position.getRequirements();
        this.responsibilities = position.getResponsibilities();
    }

    public static PositionView from(Position position) {
        return new PositionView(position);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getNumberOfPositions() {
        return numberOfPositions;
    }

    public void setNumberOfPositions(int numberOfPositions) {
        this.numberOfPositions = numberOfPositions;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    public List<String> getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(List<String> responsibilities) {
        this.responsibilities = responsibilities;
    }
}