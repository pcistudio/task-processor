package com.pcistudio.task.procesor.metrics;

public class ManagerStats {
    protected long handlersRunningCount;
    protected long handlersPausedCount;
    protected long handlersRegisteredCount;

    public long getHandlersRunningCount() {
        return handlersRunningCount;
    }

    public long getHandlersRegisteredCount() {
        return handlersRegisteredCount;
    }

    public long getHandlersPausedCount() {
        return handlersPausedCount;
    }


    public ManagerStats setHandlersRunningCount(long handlersRunningCount) {
        this.handlersRunningCount = handlersRunningCount;
        return this;
    }

    public ManagerStats setHandlersRegisteredCount(long handlersRegisteredCount) {
        this.handlersRegisteredCount = handlersRegisteredCount;
        return this;
    }

    public ManagerStats setHandlersPausedCount(long handlersPausedCount) {
        this.handlersPausedCount = handlersPausedCount;
        return this;
    }
}
