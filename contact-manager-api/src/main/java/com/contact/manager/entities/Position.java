package com.contact.manager.entities;


import com.contact.manager.entities.converter.JsonConverters;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "position_available")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String title;
    private String description;
    private String location;
    private String department;
    private int numberOfPositions;
    private int salary;
    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonConverters.StringListJsonConverter.class)
    private List<String> requirements = new ArrayList<>();
    @Convert(converter = JsonConverters.StringListJsonConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> responsibilities = new ArrayList<>();

    @OneToMany(mappedBy = "position")
    private List<Candidate> candidates;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public Position setId(Long id) {
        this.id = id;
        return this;
    }

    public String getDepartment() {
        return department;
    }

    public Position setDepartment(String department) {
        this.department = department;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Position setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Position setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public int getNumberOfPositions() {
        return numberOfPositions;
    }

    public Position setNumberOfPositions(int numberOfPositions) {
        this.numberOfPositions = numberOfPositions;
        return this;
    }

    public Position setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public Position setLocation(String location) {
        this.location = location;
        return this;
    }

    public int getSalary() {
        return salary;
    }

    public Position setSalary(int salary) {
        this.salary = salary;
        return this;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public Position setRequirements(List<String> requirements) {
        this.requirements = requirements;
        return this;
    }

    public List<String> getResponsibilities() {
        return responsibilities;
    }

    public Position setResponsibilities(List<String> responsibilities) {
        this.responsibilities = responsibilities;
        return this;
    }

}