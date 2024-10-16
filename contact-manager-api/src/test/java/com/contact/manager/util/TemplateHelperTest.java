// src/test/java/com/contact/manager/util/GenericTypeUtilTest.java
package com.contact.manager.util;

import com.contact.manager.entities.Candidate;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateHelperTest {

    @Test
    void testConvertToTemplateParams() {
        // Create a mock Candidate entity
        Candidate candidate = new Candidate();
        candidate.setId(1L);
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        candidate.setEmail("john.doe@example.com");
        candidate.setNotes(new ArrayList<>());
        candidate.setAttachments(new ArrayList<>());
        candidate.setMarkForInterview(true);

        // Call the convertToTemplateParams method
        Map<String, Object> result = TemplateHelper.convertToTemplateParams(candidate);

        // Assert that the returned map contains the expected field names and values
        assertEquals(7, result.size());
        assertTrue(result.containsKey("id"));
        assertTrue(result.containsKey("firstName"));
        assertTrue(result.containsKey("lastName"));
        assertTrue(result.containsKey("email"));

        assertEquals(1L, result.get("id"));
        assertEquals("John", result.get("firstName"));
        assertEquals("Doe", result.get("lastName"));
        assertEquals("john.doe@example.com", result.get("email"));
    }

    @Test
    void testGetObjectFieldsAndValues() {
        // Create a mock Candidate entity
        Candidate candidate = new Candidate();
        candidate.setId(1L);
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        candidate.setEmail("john.doe@example.com");
        candidate.setNotes(new ArrayList<>());
        candidate.setAttachments(new ArrayList<>());
        candidate.setMarkForInterview(true);

        // Call the convertToTemplateParams method
        Map<String, Object> result = TemplateHelper.getObjectFieldsAndValues(candidate);

        // Assert that the returned map contains the expected field names and values
        assertEquals(7, result.size());
        assertTrue(result.containsKey("id"));
        assertTrue(result.containsKey("firstName"));
        assertTrue(result.containsKey("lastName"));
        assertTrue(result.containsKey("email"));


        assertEquals(1L, result.get("id"));
        assertEquals("John", result.get("firstName"));
        assertEquals("Doe", result.get("lastName"));
        assertEquals("john.doe@example.com", result.get("email"));
    }
}