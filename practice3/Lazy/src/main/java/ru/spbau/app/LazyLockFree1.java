package ru.spbau.app;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

public class LazyLockFree1<T> implements Lazy<T> {
    private static final AtomicReferenceFieldUpdater<LazyLockFree1, Object> valueUpdater =
            AtomicReferenceFieldUpdater.newUpdater(LazyLockFree1.class, Object.class, "value");

    private volatile T value = null;
    private Supplier<T> supplier;

    public LazyLockFree1(Supplier<T> supp) {
        supplier = supp;
    }

    @Override
    public T get() {
        if (supplier != null) {
            if (valueUpdater.compareAndSet(this, null, supplier.get())) {
                supplier = null;
            }
        }
        return value;
    }
}
