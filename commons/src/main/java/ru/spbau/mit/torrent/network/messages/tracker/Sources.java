package ru.spbau.mit.torrent.network.messages.tracker;

import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.torrent.network.Address;
import ru.spbau.mit.torrent.network.messages.Message;

import java.util.List;
import java.util.Set;

public interface Sources extends Message {
    byte ID = 3;

    class Request implements Sources {
        private final int fileId;

        public Request(final int fileId) {
            this.fileId = fileId;
        }

        public int getFileId() {
            return fileId;
        }

        @NotNull
        @Override
        public String toString() {
            return String.format("SourcesRequest{fileId=%d}", fileId);
        }
    }

    class Response implements Sources {
        @NotNull
        private final Set<Address> clientsAddresses;

        public Response(@NotNull final Set<Address> clientsAddresses) {
            this.clientsAddresses = clientsAddresses;
        }

        @NotNull
        public Set<Address> getClientsAddresses() {
            return clientsAddresses;
        }

        @NotNull
        @Override
        public String toString() {
            return String.format("SourcesResponse{size=%d, clientsAddresses=%s}",
                    clientsAddresses.size(), clientsAddresses);
        }
    }
}
