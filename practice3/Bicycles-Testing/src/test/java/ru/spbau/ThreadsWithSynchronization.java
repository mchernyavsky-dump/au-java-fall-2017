package ru.spbau;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class ThreadsWithSynchronization extends AbstractConcurrentLazyTest {
    private static final int THREADS_COUNT = 400;

    @Override
    protected <T> void spamLazy(Lazy<T> lazy) {
        Thread[] threads = new Thread[THREADS_COUNT];
        CountDownLatch cdl = new CountDownLatch(THREADS_COUNT);

        for (int i = 0; i < THREADS_COUNT; i++) {
            threads[i] = new Thread(() -> {
                cdl.countDown();
                try {
                    cdl.await();
                } catch (InterruptedException ignored) { }
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
