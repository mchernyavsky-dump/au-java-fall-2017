package ru.spbau.mit.torrent.network.messages.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.mit.torrent.network.messages.Message;

import java.io.InputStream;

public interface Get extends Message {
    byte ID = 2;

    class Request implements Get {
        private final int fileId;
        private final int fileChunkId;

        public Request(final int fileId, final int fileChunkId) {
            this.fileId = fileId;
            this.fileChunkId = fileChunkId;
        }

        public int getFileId() {
            return fileId;
        }

        public int getFileChunkId() {
            return fileChunkId;
        }

        @NotNull
        @Override
        public String toString() {
            return String.format("GetRequest{fileId=%d, fileChunkId=%d}", fileId, fileChunkId);
        }
    }

    class Response implements Get {
        @Nullable
        private final InputStream fileChunkContent;
        private final int count;

        public Response(@Nullable final InputStream fileChunkContent, final int count) {
            this.fileChunkContent = fileChunkContent;
            this.count = count;
        }

        @Nullable
        public InputStream getFileChunkContent() {
            return fileChunkContent;
        }

        public int getCount() {
            return count;
        }

        @NotNull
        @Override
        public String toString() {
            return String.format("GetResponse{fileChunkContent=..., count=%d}", count);
        }
    }
}
