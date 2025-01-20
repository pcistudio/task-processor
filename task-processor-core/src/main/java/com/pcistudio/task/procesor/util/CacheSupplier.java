package com.pcistudio.task.procesor.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
public class CacheSupplier<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private final Duration cacheDuration;

    private T value;
    private Long lastUpdate;

    public CacheSupplier(Supplier<T> supplier, Duration cacheDuration) {
        this.supplier = supplier;
        this.cacheDuration = cacheDuration;
    }

    public CacheSupplier(Supplier<T> supplier) {
        this(supplier, Duration.ofSeconds(5));
    }

    @Override
    @NonNull
    public T get() {
        if (lastUpdate == null || System.currentTimeMillis() - lastUpdate > cacheDuration.toMillis()) {
            value = supplier.get();
            lastUpdate = System.currentTimeMillis();
        } else {
            log.trace("Returning cached value");
        }
        return value;
    }

    public void invalidate() {
        lastUpdate = null;
    }

    public static <T> CacheSupplier<T> from(Supplier<T> supplier, Duration cacheDuration) {
        return new CacheSupplier<>(supplier, cacheDuration);
    }

    public static <T> CacheSupplier<T> from(Supplier<T> supplier) {
        return new CacheSupplier<>(supplier);
    }
}
