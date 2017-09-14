package ru.spbau.mit;

import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class LazyFactoryTest {
    private static class SumSupplier implements Supplier<Integer> {
        private volatile int invocationsCount;
        private final int upperBound;

        private SumSupplier(int upperBound) {
            this.upperBound = upperBound;
        }


        @Override
        public Integer get() {
            int sum = 0;
            invocationsCount += 1;
            for (int i = 0; i < upperBound; i++) {
                sum += i;
            }
            return sum;
        }
    }

    @Test
    public void testCreateSingleThreadLazy() throws Exception {
        SumSupplier supplier = new SumSupplier(100_000);
        Lazy<Integer> singleThreadLazy = LazyFactory.createSingleThreadLazy(supplier);

        for (int i = 0; i < 100; i++) {
            assertEquals(704982704, (long) singleThreadLazy.get());
        }

        assertEquals(1, supplier.invocationsCount);
    }

    @Test
    public void testStupidMultiThreadLazy() throws Exception {
        final SumSupplier supplier = new SumSupplier(100_000);
        final Lazy<Integer> lazy = LazyFactory.createStupidConcurrentLazy(supplier);
        doMultiThreadTestWithLazy(lazy, supplier);
    }

    @Test
    public void testDoubleCheckLockingLazy() throws Exception {
        final SumSupplier supplier = new SumSupplier(100_000);
        final Lazy<Integer> lazy = LazyFactory.createDoubleCheckedLockingLazy(supplier);
        doMultiThreadTestWithLazy(lazy, supplier);
    }

    private <T> void doMultiThreadTestWithLazy(Lazy<T> lazy, SumSupplier supplierInsideLazy) throws Exception {
        Thread[] threads = new Thread[1000];
        final CyclicBarrier barrier = new CyclicBarrier(threads.length);

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException ignored) { }
                assertEquals(704982704, (long) (Integer) lazy.get());
            });
        }

        assertEquals(0, supplierInsideLazy.invocationsCount);

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(1, supplierInsideLazy.invocationsCount);
    }
}