package ru.spbau.test;

import java.util.function.Supplier;

public class CountingSupplier implements Supplier<Integer> {
    public int invocationsCount = 0;

    @Override
    public Integer get() {
        invocationsCount++;
        return invocationsCount;
    }
}
