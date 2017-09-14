package ru.spbau.mit.impls;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import ru.spbau.mit.Lazy;

import java.util.function.Supplier;

public class DoubleCheckedLockingLazy<T> implements Lazy<T> {
    private Supplier<T> supplier;
    private T cachedValue;

    public DoubleCheckedLockingLazy(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    @Nullable
    public T get() {
        if (supplier != null) {
            cachedValue = supplier.get();
            supplier = null;
        }
        return cachedValue;
    }
}
