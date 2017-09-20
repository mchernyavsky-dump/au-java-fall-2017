package ru.spbau.app;

import java.util.function.Supplier;

/**
 * Created by dsavv on 07.09.2016.
 */
public class LazyFactory {
    public static <T> Lazy<T> createDoubleCheckedLockingLazy(Supplier<T> supplier) {
        return new DoubleCheckedLockingLazy<>(supplier);
    }

    public static <T> Lazy<T> createThinDoubleCheckedLockingLazy(Supplier<T> supplier) {
        return new ThinDoubleCheckedLockingLazy<>(supplier);
    }

    public static <T> Lazy<T> createSynchronizedLazy(Supplier<T> supplier) {
        return new SynchronizedLazy<>(supplier);
    }

    public static <T> Lazy<T> createLockFreeLazy2(Supplier<T> supplier) {
        return new LazyLockFree2<T>(supplier);
    }
    public static <T> Lazy<T> createLockFreeLazy1(Supplier<T> supplier) {
        return new LazyLockFree1<T>(supplier);
    }
}
