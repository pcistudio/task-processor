package com.pcistudio.task.procesor.metrics;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ManagerStats {
    protected long runningHandlers;
    protected long pausedHandlers;
    protected long totalHandlers;

    public ManagerStats runningHandlers(final long runningHandlers) {
        this.runningHandlers = runningHandlers;
        return this;
    }

    public ManagerStats totalHandlers(final long totalHandlers) {
        this.totalHandlers = totalHandlers;
        return this;
    }

    public ManagerStats pausedHandlers(final long pausedHandlers) {
        this.pausedHandlers = pausedHandlers;
        return this;
    }
}
