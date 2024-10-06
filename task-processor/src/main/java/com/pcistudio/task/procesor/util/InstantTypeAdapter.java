package com.pcistudio.task.procesor.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Instant;

public class InstantTypeAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getEpochSecond()); // Epoch seconds
    }

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Instant.ofEpochSecond(json.getAsLong());
    }

}
