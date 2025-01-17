package com.pcistudio.task.procesor.metrics;

public interface TimeMeter {
    void success();

    void error(Throwable exception);

    void retry(Throwable exception);

    TimeMeter EMPTY = new EmptyTimeMeter();

    class EmptyTimeMeter implements TimeMeter {

        @Override
        public void success() {

        }

        @Override
        public void error(Throwable exception) {

        }

        @Override
        public void retry(Throwable exception) {

        }
    }
}