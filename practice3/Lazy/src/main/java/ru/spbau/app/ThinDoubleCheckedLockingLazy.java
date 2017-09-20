package ru.spbau.app;

import java.util.function.Supplier;

public class ThinDoubleCheckedLockingLazy<T> implements Lazy<T> {
    private T cachedValue = null;
    private Supplier<T> supplier;

    public ThinDoubleCheckedLockingLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (supplier != null) {
            synchronized (this) {
                if (supplier != null) {
                    cachedValue = supplier.get();
                    supplier = null;
                }
            }
        }
        return cachedValue;
    }
}

