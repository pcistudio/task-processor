package com.pcistudio.task.procesor.register;


import com.pcistudio.task.procesor.HandlerProperties;
import com.pcistudio.task.procesor.HandlerPropertiesWrapper;
import com.pcistudio.task.procesor.util.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO Centralize processor registration to avoid name collisions
// where handler tablename and application name should be unique
// the central registry should fail if `spring.application.name` is not set or unique
@Slf4j
@RequiredArgsConstructor
public class HandlerManagerImpl implements HandlerManager {
    private final Map<String, HandlerPropertiesWrapper> handlerPropertiesMap = new HashMap<>();

    private final Map<String, List<HandlerPropertiesWrapper>> handlersByTable = new HashMap<>();

    private final TaskStorageSetup taskTableSetupImpl;

    public void registerHandler(HandlerProperties handlerProperties) {
        validateHandlerProperties(handlerProperties);
        HandlerPropertiesWrapper handlerPropertiesWrapper = new HandlerPropertiesWrapper(handlerProperties);
        this.handlerPropertiesMap.put(handlerPropertiesWrapper.getHandlerName(), handlerPropertiesWrapper);
        this.handlersByTable.compute(handlerPropertiesWrapper.getTableName(), (k, v) -> {
            if (v == null) {
                v = List.of(handlerPropertiesWrapper);
            } else {
                v.add(handlerPropertiesWrapper);
            }
            return v;
        });


        taskTableSetupImpl.createStorage(handlerPropertiesWrapper.getTableName());

        log.info("Handler registered successfully for handler {}", handlerPropertiesWrapper.getHandlerName());
    }

    public HandlerPropertiesWrapper getProperties(String handlerName) {
        return handlerPropertiesMap.get(handlerName);
    }

    private void validateHandlerProperties(HandlerProperties handlerProperties) {
        if (handlerProperties.getHandlerName() == null || handlerProperties.getHandlerName().isBlank()) {
            throw new IllegalArgumentException("Handler name cannot be null or empty");
        }
        if (handlerProperties.getTableName() == null || handlerProperties.getTableName().isBlank()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
    }

    public static class Builder {
        List<HandlerProperties> handlerPropertiesList = new ArrayList<>();
        private TaskStorageSetup taskTableSetup;

        public Builder taskTableSetup(TaskStorageSetup taskTableSetup) {
            this.taskTableSetup = taskTableSetup;
            return this;
        }

        public Builder register(HandlerProperties handlerProperties) {
            handlerPropertiesList.add(handlerProperties);
            return this;
        }

        public HandlerManagerImpl build() {
            Assert.notNull(taskTableSetup, "TaskTableSetup cannot be null");
            Assert.notEmpty(handlerPropertiesList, "HandlerProperties cannot be empty");

            HandlerManagerImpl handlerManager = new HandlerManagerImpl(taskTableSetup);
            handlerPropertiesList.forEach(handlerManager::registerHandler);
            return handlerManager;
        }
    }

}
