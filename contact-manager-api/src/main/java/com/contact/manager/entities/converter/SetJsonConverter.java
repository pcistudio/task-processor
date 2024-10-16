package com.contact.manager.entities.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcistudio.task.procesor.util.GenericTypeUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Converter(autoApply = false)
public abstract class SetJsonConverter<T> implements AttributeConverter<Set<T>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Set<T> attribute) {
        try {
            if (attribute == null) {
                return null;
            }
            // Convert List<Object> to JSON string
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting list to JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Set<T> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null) {
                return new HashSet<>();
            }
            //noinspection unchecked
            Class<T> classParameterType = (Class<T>) GenericTypeUtil.getGenericTypeFromSuperclass(this.getClass());
            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(Set.class, classParameterType);
            // Convert JSON string back to List<Object>

            return Objects.requireNonNullElseGet(objectMapper.readValue(dbData, listType), HashSet::new);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting JSON to list: " + e.getMessage(), e);
        }
    }
}
