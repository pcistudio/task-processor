package com.pcistudio.task.procesor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;

public class JsonUtil {
    private static final Gson gson  = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .create();

    private JsonUtil() {
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T from(String objectStr, Class<T> clazz) {
        return gson.fromJson(objectStr, clazz);
    }
}
