package com.pcistudio.task.procesor.util.decoder;

public interface MessageDecoding {
    <T> T decode(byte[] data, Class<T> clazz);
}
