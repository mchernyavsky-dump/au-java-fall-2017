package ru.spbau.mit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class ThreadPoolNumThreadsTest {
    private final static int MIN_TIMEOUT = 1000;

    @Rule
    public Timeout timeout = new Timeout(MIN_TIMEOUT, TimeUnit.MILLISECONDS) {
        public Statement apply(Statement base, Description description) {
            return new FailOnTimeout(base, MIN_TIMEOUT) {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        super.evaluate();
                        throw new TimeoutException();
                    } catch (Exception ignored) {
                    }
                }
            };
        }
    };

    @Parameterized.Parameter
    public int numThreads;

    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[]{1, 2, 4, 8};
    }

    @Test(expected = TestTimedOutException.class)
    public void testNumThreads2() throws LightExecutionException, InterruptedException {
        final ThreadPoolImpl threadPool = new ThreadPoolImpl(numThreads);

        final Supplier<Void> threadKiller = () -> {
            Thread.currentThread().interrupt();
            return null;
        };

        for (int j = 0; j < numThreads; j++) {
            threadPool.submit(threadKiller);
        }

        final LightFuture<Integer> task = threadPool.submit(() -> 1);
        task.get();

        threadPool.shutdown();
    }
}
