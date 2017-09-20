package ru.spbau;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class NaiveTestWithThreads extends AbstractConcurrentLazyTest {
    private static final int THREADS_COUNT = 8;

    @Override
    protected <T> void spamLazy(Lazy<T> lazy) {
        Thread[] threads = new Thread[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT; i++) {
            threads[i] = new Thread(lazy::get);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) { }
        }
    }
}