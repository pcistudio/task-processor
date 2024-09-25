package com.contact.manager.util;

import org.springframework.util.Assert;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringSanitizer {

    private StringSanitizer() {
    }

    public static String sanitize(String input) {
        Assert.notNull(input, "Input string must not be null");
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

}