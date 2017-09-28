package ru.spbau.mit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static junit.framework.TestCase.*;

@RunWith(Parameterized.class)
public class ThreadPoolTest {
    private static final int TEST_REPEATS = 10;

    @Parameterized.Parameter
    public int numThreads;

    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[]{1, 2, 4, 8};
    }

    @Test
    public void testIsReady() throws LightExecutionException, InterruptedException {
        for (int i = 0; i < TEST_REPEATS; i++) {
            final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

            final LightFuture<Integer> task = threadPool.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                return 1;
            });

            assertFalse(task.isReady());
            assertEquals(new Integer(1), task.get());
            assertTrue(task.isReady());

            threadPool.shutdown();
        }
    }

    @Test
    public void testThenApply() {
        for (int i = 0; i < TEST_REPEATS; i++) {
            final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

            LightFuture<Integer> task = threadPool.submit(() -> 1);

            for (int j = 0; j < 10; j++) {
                task = task.thenApply(x -> 2 * x);
            }

            try {
                assertEquals(new Integer(1024), task.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            assertTrue(task.isReady());

            threadPool.shutdown();
        }
    }

    @Test
    public void testShutdownGet() throws LightExecutionException, InterruptedException {
        for (int i = 0; i < TEST_REPEATS; i++) {
            final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

            final LightFuture<Void> task = threadPool.submit(() -> {
                try {
                    TimeUnit.DAYS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });

            try {
                threadPool.shutdown();
                assertNotNull(task.get());
            } catch (LightExecutionException ignored) {
            }

            threadPool.shutdown();
        }
    }

    @Test
    public void testNumThreads1() throws LightExecutionException, InterruptedException {
        final ThreadPoolImpl threadPool = new ThreadPoolImpl(numThreads);

        final Supplier<Void> threadKiller = () -> {
            Thread.currentThread().interrupt();
            return null;
        };

        for (int j = 0; j < numThreads - 1; j++) {
            threadPool.submit(threadKiller);
        }

        final LightFuture<Integer> task = threadPool.submit(() -> 1);
        assertEquals(new Integer(1), task.get());

        threadPool.shutdown();
    }
}
