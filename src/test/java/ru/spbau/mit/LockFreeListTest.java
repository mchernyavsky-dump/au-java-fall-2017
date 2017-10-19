package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

public class LockFreeListTest {
    private static final int N_THREADS = 10;
    private static final int N_ELEMENTS = 1000;

    @NotNull
    private LockFreeList<Integer> list = new LockFreeListImpl<>();

    @After
    public void createNewList() {
        list = new LockFreeListImpl<>();
    }

    @Test
    public void testEmptyList() {
        assertTrue(list.isEmpty());
        assertFalse(list.contains(0));
    }

    @Test
    public void testListWithOneElement() {
        list.append(0);
        assertFalse(list.isEmpty());
        assertTrue(list.contains(0));
    }

    @Test
    public void testAppendRemoveOneElement() {
        list.append(0);
        assertTrue(list.remove(0));
        assertTrue(list.isEmpty());
        assertFalse(list.contains(0));
    }

    @Test
    public void testAppendRemoveTwoSameElements() {
        list.append(0);
        list.append(0);
        assertTrue(list.remove(0));
        assertFalse(list.isEmpty());
        assertTrue(list.contains(0));
        assertTrue(list.remove(0));
        assertTrue(list.isEmpty());
        assertFalse(list.contains(0));
    }

    @Test
    public void testAppendRemoveTwoDifferentElements() {
        list.append(0);
        list.append(1);
        assertTrue(list.remove(0));
        assertFalse(list.isEmpty());
        assertFalse(list.contains(0));
        assertTrue(list.contains(1));
        assertTrue(list.remove(1));
        assertTrue(list.isEmpty());
        assertFalse(list.contains(0));
        assertFalse(list.contains(1));
    }

    @Test
    public void testConcurrent() {
        final CyclicBarrier barrier = new CyclicBarrier(N_THREADS);
        final List<Thread> list = IntStream.range(0, N_THREADS)
                .mapToObj(i -> new Worker(i, barrier))
                .map(Thread::new)
                .peek(Thread::start)
                .collect(Collectors.toList());
        list.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        });
    }

    private class Worker implements Runnable {
        private final int value;

        @NotNull
        private final CyclicBarrier barrier;

        private Worker(final int value, @NotNull final CyclicBarrier barrier) {
            this.value = value;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                barrier.await();
            } catch (Exception e) {
                fail();
            }

            doWork();
        }

        private void doWork() {
            IntStream.range(0, N_ELEMENTS).forEach(i -> list.append(value));
            IntStream.range(0, N_ELEMENTS).forEach(i -> assertTrue(list.remove(value)));
            assertFalse(list.contains(value));
        }
    }
}
