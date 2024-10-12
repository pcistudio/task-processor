package com.pcistudio.task.procesor.register;

import com.pcistudio.task.procesor.HandlerPropertiesWrapper;

public interface ProcessorRegisterLookup {
    HandlerPropertiesWrapper getProperties(String handlerName);
}
