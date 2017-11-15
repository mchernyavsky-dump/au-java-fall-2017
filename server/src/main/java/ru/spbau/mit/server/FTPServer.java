package ru.spbau.mit.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.mit.FTPConnection;
import ru.spbau.mit.FileInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FTPServer implements AutoCloseable {
    private final int port;

    @NotNull
    private final Path root;
    @NotNull
    private final ExecutorService service = Executors.newCachedThreadPool();
    @Nullable
    private volatile ServerSocket serverSocket;

    public FTPServer(final int port, @NotNull final Path root) throws IOException {
        this.port = port;
        this.root = root;
    }

    public void start() throws IOException {
        if (serverSocket != null) {
            throw new IllegalStateException("Server already started");
        }

        serverSocket = new ServerSocket(port);
        service.submit(new ConnectionsListener());
    }

    public void stop() throws IOException {
        if (serverSocket == null) {
            throw new IllegalStateException("Server not started");
        }

        serverSocket.close();
        service.shutdown();
    }

    public void close() throws IOException {
        stop();
    }

    private class ConnectionsListener implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    service.submit(new ConnectionHandler(clientSocket));
                } catch (IOException ignore) {
                }
            }
        }
    }

    private class ConnectionHandler implements Runnable {
        @NotNull
        final Socket socket;

        public ConnectionHandler(@NotNull final Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (FTPServerConnection connection = new FTPServerConnection(socket)) {
                int requestType = connection.receiveRequestType();
                switch (requestType) {
                    case FTPConnection.REQUEST_TYPE_LIST:
                        handleListRequest(connection);
                        break;
                    case FTPConnection.REQUEST_TYPE_GET:
                        handleGetRequest(connection);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown request type");
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        private void handleListRequest(@NotNull final FTPServerConnection connection) throws IOException {
            final Path directoryPath = root.resolve(connection.receiveListRequestBody());
            try {
                final List<FileInfo> files = Files.list(directoryPath)
                        .map(file -> new FileInfo(file.getFileName().toString(), Files.isDirectory(file)))
                        .collect(Collectors.toList());
                connection.sendListResponse(files);
            } catch (Exception e) {
                connection.sendListResponse(Collections.emptyList());
            }
        }

        private void handleGetRequest(@NotNull final FTPServerConnection connection) throws IOException {
            final Path filePath = root.resolve(connection.receiveGetRequestBody());
            connection.sendGetResponse(filePath.toFile());
        }
    }
}
