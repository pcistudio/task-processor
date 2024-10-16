package com.contact.manager.entities.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcistudio.task.procesor.util.GenericTypeUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Converter(autoApply = false)
public abstract class ListJsonConverter<T> implements AttributeConverter<List<T>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<T> attribute) {
        try {
            // Convert List<Object> to JSON string
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting list to JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public List<T> convertToEntityAttribute(String dbData) {
        try {
            //noinspection unchecked
            Class<T> classParameterType = (Class<T>) GenericTypeUtil.getGenericTypeFromSuperclass(this.getClass());
            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, classParameterType);
            // Convert JSON string back to List<Object>
            return Objects.requireNonNullElseGet(objectMapper.readValue(dbData, listType), ArrayList::new);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting JSON to list: " + e.getMessage(), e);
        }
    }
}
