package ru.spbau;

import java.util.function.Supplier;

public class CountingSupplier implements Supplier<Integer>  {
    private static final int SLEEP_TIME_MS = 200;

    public int invocationsCount = 0;

    @Override
    public Integer get() {
        // Simulate long-computing task
        invocationsCount++;
        return invocationsCount;
    }
}
