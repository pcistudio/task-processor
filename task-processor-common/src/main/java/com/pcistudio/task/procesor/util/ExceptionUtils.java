package com.pcistudio.task.procesor.util;

import edu.umd.cs.findbugs.annotations.Nullable;

public final class ExceptionUtils {
    private ExceptionUtils() {
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    @Nullable
    public static Throwable getRootCause(Throwable original) {
        if (original == null) {
            return null;
        }
        Throwable rootCause = null;
        Throwable cause = original.getCause();
        while (cause != null && cause != rootCause) {
            rootCause = cause;
            cause = cause.getCause();
        }
        return rootCause;
    }

    public static Throwable getMostSpecificCause(Throwable original) {
        Throwable rootCause = getRootCause(original);
        return rootCause != null ? rootCause : original;
    }

    public static Throwable unwrapException(Exception exception) {
        return exception.getCause() == null ? exception : exception.getCause();
    }


}
