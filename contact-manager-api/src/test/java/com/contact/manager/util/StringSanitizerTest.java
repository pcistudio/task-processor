package com.contact.manager.util;

import static org.junit.jupiter.api.Assertions.*;

class StringSanitizerTest {

    @org.junit.jupiter.api.Test
    void sanitize() {
        String input = "áéíóú";
        String expected = "aeiou";
        String actual = StringSanitizer.sanitize(input);
        assertEquals(expected, actual);
    }
}