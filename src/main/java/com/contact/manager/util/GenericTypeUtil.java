package com.contact.manager.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GenericTypeUtil {


    private GenericTypeUtil() {
    }

    public static Class<?> getGenericTypeFromSuperclass(Class<?> clazz) {
        return getGenericTypeFromSuperclass(clazz, 0);
    }

    // This method extracts the generic type of the class that extends the superclass
    public static Class<?> getGenericTypeFromSuperclass(Class<?> clazz, int index) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        // Check if it's a parameterized type (i.e., a class with generics)
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {

            // Get the actual type arguments (generics) of the superclass
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (index >= 0 && index < typeArguments.length) {
                // Return the Class of the specified generic type
                return (Class<?>) typeArguments[index];
            }
        }

        throw new IllegalArgumentException("No generic type found at index: " + index);
    }

    public static Map<String, Object> getObjectFieldsAndValues(Object obj) {
        Map<String, Object> fieldMap = new HashMap<>();
        Class<?> clazz = obj.getClass();
        BeanWrapper object = new BeanWrapperImpl(obj);
        for (Field field : clazz.getDeclaredFields()) {
            Object propertyValue = object.getPropertyValue(field.getName());
            if (propertyValue != null) {
                fieldMap.put(field.getName(), propertyValue);
            }
        }

        return fieldMap;
    }

    public static Map<String, Object> convertToTemplateParams(Object ... objs) {
        Map<String, Object> fieldMap = new HashMap<>();

        for (Object object : objs) {
            if (object instanceof Map map) {
                try {
                    fieldMap.putAll(map);
                } catch (ClassCastException e) {
                    log.error("Error casting map to Map<String, Object>", e);
                }
                continue;
            }
            //This can cause name collision and lost of parameter values
            fieldMap.putAll(getObjectFieldsAndValues(object));
        }
        return fieldMap;
    }
}
