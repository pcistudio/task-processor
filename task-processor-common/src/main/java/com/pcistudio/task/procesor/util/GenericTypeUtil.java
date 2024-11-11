package com.pcistudio.task.procesor.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
            return (Class<?>) parameterizedType.getActualTypeArguments()[index];
            // Get the actual type arguments (generics) of the superclass
//            Type[] typeArguments = parameterizedType.getActualTypeArguments();
//
//            if (index >= 0 && index < typeArguments.length) {
//                // Return the Class of the specified generic type
//                return (Class<?>) typeArguments[index];
//            }
        }

        throw new IllegalArgumentException("No generic type found at index: " + index);
    }

    public static Class<?> getGenericTypeFromInterface(Class<?> clazz, Class<?> type) {
        return getGenericTypeFromInterface(clazz, type, 0);
    }

    // This method extracts the generic type of the class that extends the superclass
    public static Class<?> getGenericTypeFromInterface(Class<?> clazz, Class<?> type, int index) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        // Check if it's a parameterized type (i.e., a class with generics)
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType parameterizedType) {
                if (type == parameterizedType.getRawType()) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    return (Class<?>) actualTypeArguments[index];
                }
            }
        }
        throw new IllegalArgumentException("No generic type found at index: " + index);
    }
}
