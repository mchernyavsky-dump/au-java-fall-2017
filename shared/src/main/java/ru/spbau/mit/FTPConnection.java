package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class FTPConnection implements AutoCloseable {
    public static final int REQUEST_TYPE_LIST = 1;
    public static final int REQUEST_TYPE_GET = 2;

    @NotNull
    protected final DataInputStream in;
    @NotNull
    protected final DataOutputStream out;
    @NotNull
    private final Socket socket;

    public FTPConnection(@NotNull final Socket socket) throws IOException {
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
