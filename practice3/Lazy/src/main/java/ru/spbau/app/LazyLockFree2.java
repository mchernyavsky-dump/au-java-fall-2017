package ru.spbau.app;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

public class LazyLockFree2<T> implements Lazy<T> {
    private static final AtomicReferenceFieldUpdater<LazyLockFree2, Object> valueUpdater =
            AtomicReferenceFieldUpdater.newUpdater(LazyLockFree2.class, Object.class, "value");

    private volatile T value = null;
    private Supplier<T> supplier;

    public LazyLockFree2(Supplier<T> supp) {
        supplier = supp;
    }

    @Override
    public T get() {
        Supplier<T> tmpSupplier = supplier;
        if (supplier != null) {
            if (valueUpdater.compareAndSet(this, null, tmpSupplier.get())) {
                supplier = null;
            }
        }
        return value;
    }
}
