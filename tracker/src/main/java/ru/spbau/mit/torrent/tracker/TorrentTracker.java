package ru.spbau.mit.torrent.tracker;

import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.torrent.AbstractServer;
import ru.spbau.mit.torrent.Constants;
import ru.spbau.mit.torrent.network.Address;
import ru.spbau.mit.torrent.network.Tracker2ClientConnection;
import ru.spbau.mit.torrent.network.messages.tracker.List;
import ru.spbau.mit.torrent.network.messages.tracker.Sources;
import ru.spbau.mit.torrent.network.messages.tracker.Update;
import ru.spbau.mit.torrent.network.messages.tracker.Upload;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TorrentTracker extends AbstractServer {
    @NotNull
    private final Path rootFolderPath;
    @NotNull
    private final TorrentTrackerState state = new TorrentTrackerState();

    public TorrentTracker(@NotNull final Path rootFolderPath)
            throws IOException, ClassNotFoundException {
        super(Constants.TRACKER_PORT);
        this.rootFolderPath = rootFolderPath;
        state.restore(rootFolderPath);
    }

    public void close() throws IOException {
        super.close();
        state.save(rootFolderPath);
    }

    @NotNull
    @Override
    protected Runnable getConnectionHandler(@NotNull final Socket clientSocket) {
        return new ConnectionHandler(clientSocket);
    }

    private class ConnectionHandler implements Runnable {
        @NotNull
        final Socket socket;

        public ConnectionHandler(@NotNull final Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (Tracker2ClientConnection connection = new Tracker2ClientConnection(socket)) {
                int requestType = connection.receiveRequestType();
                switch (requestType) {
                    case List.ID:
                        handleListRequest(connection);
                        break;
                    case Upload.ID:
                        handleUploadRequest(connection);
                        break;
                    case Sources.ID:
                        handleSourcesRequest(connection);
                        break;
                    case Update.ID:
                        handleUpdateRequest(connection);
                        break;
                    default:
                        throw new IOException("Unknown request type");
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        private void handleListRequest(@NotNull final Tracker2ClientConnection connection)
                throws IOException {
//            final List.Request request = connection.receiveListRequest();
            final List.Response response = new List.Response(state.getFileInfos());
            connection.sendListResponse(response);
        }

        private void handleUploadRequest(@NotNull final Tracker2ClientConnection connection)
                throws IOException {
            final Upload.Request request = connection.receiveUploadRequest();
            final int fileId = state.addNewFile(request.getFileName(), request.getFileSize());
            final Upload.Response response = new Upload.Response(fileId);
            connection.sendUploadResponse(response);
        }

        private void handleSourcesRequest(@NotNull final Tracker2ClientConnection connection)
                throws IOException {
            final Sources.Request request = connection.receiveSourcesRequest();
            final int fileId = request.getFileId();
            final Set<Address> seederAddresses = state.getSeeders(fileId);
            final Sources.Response response = new Sources.Response(seederAddresses);
            connection.sendSourcesResponse(response);
        }

        private void handleUpdateRequest(@NotNull final Tracker2ClientConnection connection)
                throws IOException {
            final Update.Request request = connection.receiveUpdateRequest();
            final Address seederPort = new Address(socket.getInetAddress().getAddress(),
                    request.getClientPort());
            for (int fileId : request.getFilesIds()) {
                state.addSeeder(fileId, seederPort);
            }
            final Update.Response response = new Update.Response(true);  // TODO: false?
            connection.sendUpdateResponse(response);
        }
    }
}
