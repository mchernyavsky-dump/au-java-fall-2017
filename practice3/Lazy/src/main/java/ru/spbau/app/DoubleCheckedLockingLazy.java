package ru.spbau.app;

import java.util.function.Supplier;

public class DoubleCheckedLockingLazy<T> implements Lazy<T> {
    private T cachedValue = null;
    private Supplier<T> supplier;
    private boolean isComputed = false;

    public DoubleCheckedLockingLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (!isComputed) {
            synchronized (this) {
                if (!isComputed) {
                    cachedValue = supplier.get();
                    isComputed = true;
                }
            }
        }
        return cachedValue;
    }
}
