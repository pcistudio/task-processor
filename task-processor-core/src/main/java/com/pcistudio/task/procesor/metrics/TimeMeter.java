package com.pcistudio.task.procesor.metrics;

public interface TimeMeter {
    TimeMeter EMPTY = new EmptyTimeMeter();

    void success();

    void error(Throwable exception);

    void retry(Throwable exception);

    String ERROR_TAG = "exception";
    String OUTCOME_TAG = "outcome";

    class EmptyTimeMeter implements TimeMeter {

        @Override
        public void success() {
            //Use when metrics are not been recorded
        }

        @Override
        public void error(Throwable exception) {
            //Use when metrics are not been recorded
        }

        @Override
        public void retry(Throwable exception) {
            //Use when metrics are not been recorded
        }
    }
}