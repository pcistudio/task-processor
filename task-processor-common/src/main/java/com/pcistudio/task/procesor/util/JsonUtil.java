package com.pcistudio.task.procesor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pcistudio.task.procesor.page.Cursor;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .create();

    private JsonUtil() {
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static byte[] toJsonBytes(Object object) {
        return gson.toJson(object).getBytes(StandardCharsets.UTF_8);
    }

    public static <T> T from(String objectStr, Class<T> clazz) {
        return gson.fromJson(objectStr, clazz);
    }

    public static <T> T fromJson(String json, Class<T> clazz, Class<?>... genericTypes) {
        return gson.fromJson(json, TypeToken.getParameterized(clazz, genericTypes).getType());
    }
}
