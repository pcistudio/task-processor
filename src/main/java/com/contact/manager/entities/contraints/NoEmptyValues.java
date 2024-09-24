package com.contact.manager.entities.contraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NoEmptyValuesValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoEmptyValues {
    String message() default "Collection must not contain empty values";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}