package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface ThreadPool {

    void shutdown();

    @NotNull
    <T> LightFuture<T> submit(@NotNull final Supplier<T> supplier);
}
