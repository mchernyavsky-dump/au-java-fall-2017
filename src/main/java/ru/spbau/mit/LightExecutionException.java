package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;

public class LightExecutionException extends Exception {

    public LightExecutionException() {
    }

    public LightExecutionException(@NotNull final String message) {
        super(message);
    }

    public LightExecutionException(@NotNull final String message, @NotNull final Throwable cause) {
        super(message, cause);
    }

    public LightExecutionException(@NotNull final Throwable cause) {
        super(cause);
    }
}
