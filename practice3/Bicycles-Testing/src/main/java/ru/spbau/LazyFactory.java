package ru.spbau;

import java.util.function.Supplier;

public class LazyFactory {
    public static <T> Lazy<T> createConcurrentLazy(Supplier<T> supplier) {
        return new NotReallyConcurrentLazy<>(supplier);
    }
}
