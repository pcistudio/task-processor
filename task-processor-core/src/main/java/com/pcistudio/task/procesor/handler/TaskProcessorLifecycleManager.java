package com.pcistudio.task.procesor.handler;

public interface TaskProcessorLifecycleManager {
    void start(String handlerName);
    void close(String handlerName);
//    void pause(String handlerName);
    void restart(String handlerName);

    void close();
    void start();
}