package ru.spbau.test.lockfree;

import ru.spbau.app.Lazy;
import ru.spbau.app.LazyFactory;
import ru.spbau.test.CountingSupplier;

import java.util.concurrent.CountDownLatch;

public class NaiveTest {
    private static final int THREADS_COUNT = 1000;

    public static void main(String[] args) {
        for (int i = 0; i < 100000; i++) {
            CountingSupplier countingSupplier = new CountingSupplier();
            Lazy<Integer> lazy = LazyFactory.createLockFreeLazy1(countingSupplier);

            spamLazy(lazy);
        }
    }


    protected static <T> void spamLazy(Lazy<T> lazy) {
        Thread[] threads = new Thread[THREADS_COUNT];
        CountDownLatch cdl = new CountDownLatch(THREADS_COUNT);

        for (int i = 0; i < THREADS_COUNT; i++) {
            threads[i] = new Thread(() -> {
                cdl.countDown();
                try {
                    cdl.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lazy.get();
            });
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