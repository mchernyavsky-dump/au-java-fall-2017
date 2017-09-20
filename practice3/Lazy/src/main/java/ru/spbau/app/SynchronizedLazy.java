package ru.spbau.app;

import java.util.function.Supplier;

public class SynchronizedLazy<T> implements Lazy<T> {
    private T cachedValue = null;
    private Supplier<T> supplier;

    public SynchronizedLazy(Supplier<T> supp) {
        supplier = supp;
    }

    @Override
    synchronized public T get() {
        if (supplier != null) {
            cachedValue = supplier.get();
            supplier = null;
        }
        return cachedValue;
    }
}
