package com.pcistudio.task.procesor.util;


import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Collection;


public final class Assert {
    private Assert() {
    }


    public static void notNull(@Nullable Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(@Nullable Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
