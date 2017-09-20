package ru.spbau;

import java.util.function.Supplier;

public class NotReallyConcurrentLazy<T> implements Lazy<T> {
    private final Supplier<T> supplier;
    private T cachedValue;
    private boolean isComputed = false;


    public NotReallyConcurrentLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (!isComputed) {
            cachedValue = supplier.get();
            isComputed = true;
        }
        return cachedValue;
    }
}
