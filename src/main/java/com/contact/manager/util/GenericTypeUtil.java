package com.contact.manager.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
}
