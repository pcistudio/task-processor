package com.pcistudio.task.procesor.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Slf4j
public final class GenericTypeUtil {


    private GenericTypeUtil() {
    }

    public static Class<?> getGenericTypeFromSuperclass(Class<?> clazz) {
        return getGenericTypeFromSuperclass(clazz, 0);
    }

    // This method extracts the generic type of the class that extends the superclass
    public static Class<?> getGenericTypeFromSuperclass(Class<?> clazz, int index) {
        Type genericSuperclass = clazz.getGenericSuperclass();

        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            Assert.isTrue(index >= 0 && index < parameterizedType.getActualTypeArguments().length, "No generic type found at index: " + index);

            return (Class<?>) parameterizedType.getActualTypeArguments()[index];
        }

        throw new IllegalArgumentException("No ParameterizedType class=" + clazz.getCanonicalName());
    }

    public static Class<?> getGenericTypeFromInterface(Class<?> clazz, Class<?> type) {
        return getGenericTypeFromInterface(clazz, type, 0);
    }

    // This method extracts the generic type of the class that extends the superclass
    public static Class<?> getGenericTypeFromInterface(Class<?> clazz, Class<?> type, int index) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        // Check if it's a parameterized type
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType parameterizedType && parameterizedType.getRawType() == type) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                return (Class<?>) actualTypeArguments[index];
            }
        }
        throw new IllegalArgumentException("No generic type found at index: " + index);
    }
}
