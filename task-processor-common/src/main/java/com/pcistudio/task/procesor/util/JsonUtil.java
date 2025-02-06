package com.pcistudio.task.procesor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
public final class JsonUtil {
    private static final int KB = 1024;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .create();

    private static final Gson gsonPretty = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .setPrettyPrinting()
            .create();

    private JsonUtil() {
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static String toPrettyJson(Object object) {
        return gsonPretty.toJson(object);
    }

    public static byte[] toJsonBytes(Object object) {
        String json = gson.toJson(object);
        if(log.isTraceEnabled()) {
            log.trace("Json from object: {}", json.substring(0, Math.min(KB, json.length())));
        }
        return json.getBytes(StandardCharsets.UTF_8);
    }

    public static <T> T from(byte[] objectBytes, Class<T> clazz) {
        return from(new String(objectBytes, StandardCharsets.UTF_8), clazz);
    }

    public static <T> T from(String objectStr, Class<T> clazz) {
        if(log.isTraceEnabled()) {
            log.trace("Object from json: {}", objectStr.substring(0, Math.min(KB, objectStr.length())));
        }
        return gson.fromJson(objectStr, clazz);
    }

    public static <T> T fromJson(String json, Class<T> clazz, Class<?>... genericTypes) {
        return gson.fromJson(json, TypeToken.getParameterized(clazz, genericTypes).getType());
    }

    public static <T> T fromJson(byte[] json, Class<T> clazz, Class<?>... genericTypes) {
        return fromJson(new String(json, StandardCharsets.UTF_8), clazz, genericTypes);
    }

    public static void print(String message, Object object) { // this print didn't work with lamdas
        Logger logger = callerLogger();
        if (logger.isInfoEnabled()) {
            logger.info("{} {}", message, gsonPretty.toJson(object));
        }
    }

    public static void print(Object object) { // this print didn't work with lamdas
        Logger logger = callerLogger();
        if (logger.isInfoEnabled()) {
            logger.info(gsonPretty.toJson(object));
        }
    }

    private static Logger callerLogger() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 4) {
            log.warn("Couldn't get the caller logger");
            return log;
        }
        try {
            StackTraceElement stackTraceElement = stackTrace[3];
            Class<?> callerClass = Class.forName(stackTraceElement.getClassName());
            return LoggerFactory.getLogger(callerClass);
        } catch (Exception e) {
            log.warn("Error getting the caller logger", e);
            return log;
        }

    }
}
