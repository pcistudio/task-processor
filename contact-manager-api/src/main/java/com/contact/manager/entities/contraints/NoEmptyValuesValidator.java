package com.contact.manager.entities.contraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

public class NoEmptyValuesValidator implements ConstraintValidator<NoEmptyValues, Collection<String>> {

    @Override
    public boolean isValid(Collection<String> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Consider null as valid, use @NotNull for null check
        }
        return value.stream().noneMatch(String::isEmpty);
    }
}