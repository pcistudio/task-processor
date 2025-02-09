package com.pcistudio.task.procesor.register;


import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.util.Assert;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//TODO Centralize processor registration to avoid name collisions
// where handler tablename and application name should be unique
// the central registry should fail if `spring.application.name` is not set or unique
public final class HandlerManagerImpl implements HandlerManager {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HandlerManagerImpl.class);

    private final Map<String, HandlerPropertiesWrapper> propertiesMap = new ConcurrentHashMap<>();

    private final Map<String, List<HandlerPropertiesWrapper>> handlersByTable = new ConcurrentHashMap<>();

    private final TaskStorageSetup taskTableSetup;

    private HandlerManagerImpl(final Builder builder) {
        Assert.notNull(builder.taskTableSetup, "TaskTableSetup cannot be null");
        this.taskTableSetup = builder.taskTableSetup;
    }

    @Override
    public void registerHandler(final HandlerProperties handlerProperties) {
        validateHandlerProperties(handlerProperties);
        final HandlerPropertiesWrapper properties = new HandlerPropertiesWrapper(handlerProperties);
        if (propertiesMap.containsKey(properties.getHandlerName())) {
            throw new IllegalStateException("Handler already registered: " + properties.getHandlerName());
        }
        this.propertiesMap.put(properties.getHandlerName(), properties);
        this.handlersByTable.compute(properties.getTableName(), (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            v.add(properties);
            return v;
        });


        taskTableSetup.createStorage(properties.getTableName());

        log.info("Handler registered successfully for handler {}", properties.getHandlerName());//NOPMD
    }

    @Override
    public HandlerPropertiesWrapper getProperties(final String handlerName) {
        return propertiesMap.get(handlerName);
    }

    @Override
    public Iterator<HandlerPropertiesWrapper> getIterator() {
        return Collections.unmodifiableMap(propertiesMap).values().iterator();
    }

    private void validateHandlerProperties(final HandlerProperties handlerProperties) {
        if (handlerProperties.getHandlerName().isBlank()) {
            throw new IllegalArgumentException("Handler name cannot be null or empty");
        }
        if (handlerProperties.getTableName().isBlank()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<HandlerProperties> propertiesList = new ArrayList<>();
        @Nullable
        private TaskStorageSetup taskTableSetup;

        public Builder taskTableSetup(final TaskStorageSetup taskTableSetup) {
            this.taskTableSetup = taskTableSetup;
            return this;
        }

        public Builder register(final HandlerProperties handlerProperties) {
            propertiesList.add(handlerProperties);
            return this;
        }

        public HandlerManagerImpl build() {
            Assert.notEmpty(propertiesList, "HandlerProperties cannot be empty");

            final HandlerManagerImpl handlerManager = new HandlerManagerImpl(this);
            propertiesList.forEach(handlerManager::registerHandler);
            return handlerManager;
        }
    }

}
