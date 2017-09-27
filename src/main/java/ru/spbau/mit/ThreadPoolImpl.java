package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ThreadPoolImpl implements ThreadPool {

    @NotNull
    private final Collection<Thread> workers;

    @NotNull
    private final Queue<LightFutureImpl<?>> tasks = new ArrayDeque<>();

    public ThreadPoolImpl(final int n) {
        workers = IntStream.range(0, n)
                .mapToObj(i -> new Worker())
                .map(Thread::new)
                .peek(Thread::start)
                .collect(Collectors.toList());
    }

    @Override
    public void shutdown() {
        workers.forEach(Thread::interrupt);
        synchronized (tasks) {
            tasks.forEach(LightFutureImpl::cancel);
            tasks.clear();
        }
    }

    @NotNull
    @Override
    public <T> LightFuture<T> submit(@NotNull final Supplier<T> supplier) {
        final LightFutureImpl<T> task = new LightFutureImpl<>(supplier);
        return submit(task);
    }

    @NotNull
    private <T> LightFuture<T> submit(@NotNull final LightFutureImpl<T> task) {
        synchronized (tasks) {
            tasks.add(task);
            tasks.notify();
        }
        return task;
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    final LightFutureImpl<?> task;
                    synchronized (tasks) {
                        while (tasks.isEmpty()) {
                            wait();
                        }
                        task = tasks.remove();
                    }
                    task.execute();
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    private class LightFutureImpl<T> implements LightFuture<T> {

        @NotNull
        private final Collection<LightFutureImpl<?>> dependentTasks = new ArrayList<>();

        @NotNull
        private Supplier<T> supplier;

        private volatile boolean readyFlag;

        @Nullable
        private T result;

        @Nullable
        private LightExecutionException exception;

        private LightFutureImpl(@NotNull final Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public boolean isReady() {
            return readyFlag;
        }

        @Nullable
        @Override
        public T get() throws LightExecutionException, InterruptedException {
            if (!isReady()) {
                synchronized (this) {
                    while (!isReady()) {
                        wait();
                    }
                }
            }

            if (exception != null) {
                throw exception;
            }

            return result;
        }

        @NotNull
        @Override
        public <R> LightFuture<R> thenApply(@NotNull final Function<? super T, ? extends R> function) {
            final LightFutureImpl<R> task = new LightFutureImpl<>(() -> {
                try {
                    return function.apply(get());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            synchronized (dependentTasks) {
                dependentTasks.add(task);
            }

            return task;
        }

        private void execute() {
            try {
                setResult(supplier.get());
            } catch (Exception e) {
                setException(new LightExecutionException(e));
            }

            synchronized (dependentTasks) {
                dependentTasks.forEach(ThreadPoolImpl.this::submit);
                dependentTasks.clear();
            }
        }

        private void cancel() {
            synchronized (dependentTasks) {
                dependentTasks.forEach(LightFutureImpl::cancel);
                dependentTasks.clear();
            }

            setException(new LightExecutionException("The task was canceled"));
        }

        private void setResult(@Nullable final T result) {
            this.result = result;
            readyFlag = true;
            notifyAll();
        }

        private void setException(@NotNull final LightExecutionException exception) {
            this.exception = exception;
            readyFlag = true;
            notifyAll();
        }
    }
}
