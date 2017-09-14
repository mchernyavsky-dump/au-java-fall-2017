package ru.spbau.mit;

import ru.spbau.mit.impls.DoubleCheckedLockingLazy;
import ru.spbau.mit.impls.SingleThreadLazyImpl;
import ru.spbau.mit.impls.StupidConcurrentLazy;

import java.util.function.Supplier;

public class LazyFactory {
    public static <T> Lazy<T> createSingleThreadLazy(Supplier<T> supplier) {
        return new SingleThreadLazyImpl<>(supplier);
    }

    public static <T> Lazy<T> createStupidConcurrentLazy(Supplier<T> supplier) {
        return new StupidConcurrentLazy<>(supplier);
    }

    public static <T> Lazy<T> createDoubleCheckedLockingLazy(Supplier<T> supplier) {
        return new DoubleCheckedLockingLazy<>(supplier);
    }
}
