package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface LightFuture<T> {

    boolean isReady();

    @Nullable
    T get() throws LightExecutionException, InterruptedException;

    @NotNull
    <R> LightFuture<R> thenApply(@NotNull final Function<? super T, ? extends R> function);
}
