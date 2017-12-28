package ru.spbau.mit.torrent.network.messages.tracker;

import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.torrent.filesystem.FileInfo;
import ru.spbau.mit.torrent.network.messages.Message;

public interface List extends Message {
    byte ID = 1;

    class Request implements List {
        @NotNull
        @Override
        public String toString() {
            return "ListRequest{}";
        }
    }

    class Response implements List {
        @NotNull
        private final java.util.List<FileInfo> fileInfos;

        public Response(@NotNull final java.util.List<FileInfo> fileInfos) {
            this.fileInfos = fileInfos;
        }

        @NotNull
        public java.util.List<FileInfo> getFileInfos() {
            return fileInfos;
        }

        @NotNull
        @Override
        public String toString() {
            return String.format("ListResponse{count=%d, fileInfos=%s}", fileInfos.size(), fileInfos);
        }
    }
}
