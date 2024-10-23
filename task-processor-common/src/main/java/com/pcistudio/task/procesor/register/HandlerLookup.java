package com.pcistudio.task.procesor.register;

import com.pcistudio.task.procesor.HandlerPropertiesWrapper;

import java.util.Iterator;


public interface HandlerLookup {
    HandlerPropertiesWrapper getProperties(String handlerName);
    Iterator<HandlerPropertiesWrapper> getIterator();
}
