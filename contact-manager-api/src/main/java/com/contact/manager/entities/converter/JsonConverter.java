package com.contact.manager.entities.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcistudio.task.procesor.util.GenericTypeUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false) // Use autoApply=true if you want this to be applied to all object-to-JSON conversions
public abstract class JsonConverter<T> implements AttributeConverter<T, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(T attribute) {
        try {
            // Convert Object to JSON String
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting object to JSON string: " + e.getMessage(), e);
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        try {
            //noinspection unchecked
            Class<T> classParameterType = (Class<T>) GenericTypeUtil.getGenericTypeFromSuperclass(this.getClass());
            return objectMapper.readValue(dbData, classParameterType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON string to object: " + e.getMessage(), e);
        }
    }
}