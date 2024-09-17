// src/main/java/com/contact/manager/validation/AtLeastOneFieldValidator.java
package com.contact.manager.entities.contraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class AtLeastOneFieldValidator implements ConstraintValidator<AtLeastOneField, Object> {

    private static final Logger log = LoggerFactory.getLogger(AtLeastOneFieldValidator.class);
    private String[] fields;

    @Override
    public void initialize(AtLeastOneField constraintAnnotation) {
        this.fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapper object = new BeanWrapperImpl(value);
        for (String fieldName : fields) {
            try {
                Object fieldValue = object.getPropertyValue(fieldName);
                if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                    return true;
                }
            } catch (Exception e) {
                log.error("Error while validating field={}", fieldName, e);
                return false;
            }
        }

        return false;
    }
}