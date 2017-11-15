package ru.spbau.mit.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.mit.FileInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

public class FTPClient implements AutoCloseable {
    @Nullable
    private volatile FTPClientConnection connection;

    public void connect(@NotNull final String host, final int port) throws IOException {
        if (connection != null) {
            throw new IllegalStateException("Connection already established");
        }

        final Socket socket = new Socket(host, port);
        connection = new FTPClientConnection(socket);
    }

    public void disconnect() throws IOException {
        if (connection == null) {
            throw new IllegalStateException("Connection not established");
        }

        connection.close();
    }

    @NotNull
    public List<FileInfo> executeList(@NotNull final String path) throws IOException {
        if (connection == null) {
            throw new IllegalStateException("Connection not established");
        }

        connection.sendListRequest(path);
        return connection.receiveListResponse();
    }

    @NotNull
    public InputStream executeGet(@NotNull final String path) throws IOException {
        if (connection == null) {
            throw new IllegalStateException("Connection not established");
        }

        connection.sendGetRequest(path);
        return connection.receiveGetResponse();
    }

    @Override
    public void close() throws IOException {
        disconnect();
    }
}
