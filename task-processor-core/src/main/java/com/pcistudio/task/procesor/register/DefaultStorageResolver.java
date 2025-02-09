package com.pcistudio.task.procesor.register;

import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.StorageResolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultStorageResolver implements StorageResolver {

    private final HandlerLookup handlerLookup;

    @Override
    public String resolveStorageName(final String handlerName) {
        final HandlerPropertiesWrapper properties = handlerLookup.getProperties(handlerName);
        if (properties == null) {
            throw new IllegalArgumentException("Handler not found: " + handlerName);
        }
        return properties.getTableName();
    }

    @Override
    public String resolveErrorStorageName(final String handlerName) {
        return resolveStorageName(handlerName) + "_error";
    }
}
