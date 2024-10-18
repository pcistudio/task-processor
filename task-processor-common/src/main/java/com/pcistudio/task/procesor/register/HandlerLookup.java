package com.pcistudio.task.procesor.register;

import com.pcistudio.task.procesor.HandlerPropertiesWrapper;


public interface HandlerLookup {
    HandlerPropertiesWrapper getProperties(String handlerName);
}
