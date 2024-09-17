package com.contact.manager.entities.contraints;

import com.contact.manager.entities.Candidate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AtLeastOneFieldValidatorTest {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        validator = factory.getValidator();
    }

    @AfterAll
    public static void close() {
        factory.close();
    }

    @Test
    void testAtLeastOneFieldValid() {
        Candidate candidate = new Candidate();
        candidate.setFirstName("John");
        candidate.setOfficePhone("123456789");

        Set<ConstraintViolation<Candidate>> violations = validator.validate(candidate);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testAtLeastOneFieldInvalid() {
        Candidate candidate = new Candidate();
        candidate.setFirstName("John");

        Set<ConstraintViolation<Candidate>> violations = validator.validate(candidate);
        assertEquals(1, violations.size());

        ConstraintViolation<Candidate> violation = violations.iterator().next();
        assertEquals("At least one of the fields must be present", violation.getMessage());
    }
}