package ru.spbau.mit.client;

import org.apache.commons.io.input.BoundedInputStream;
import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.FTPConnection;
import ru.spbau.mit.FileInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FTPClientConnection extends FTPConnection {

    public FTPClientConnection(@NotNull final Socket socket) throws IOException {
        super(socket);
    }

    public void sendListRequest(@NotNull final String path) throws IOException {
        out.writeInt(REQUEST_TYPE_LIST);
        out.writeUTF(path);
        out.flush();
    }

    public void sendGetRequest(@NotNull final String path) throws IOException {
        out.writeInt(REQUEST_TYPE_GET);
        out.writeUTF(path);
        out.flush();
    }

    @NotNull
    public List<FileInfo> receiveListResponse() throws IOException {
        final Supplier<FileInfo> filesSupplier = () -> {
            try {
                return FileInfo.read(in);
            } catch (IOException e) {
                throw new RuntimeException(e);  // :(
            }
        };
        final int filesNumber = in.readInt();
        return Stream.generate(filesSupplier)
                .limit(filesNumber)
                .collect(Collectors.toList());
    }

    @NotNull
    public InputStream receiveGetResponse() throws IOException {
        long size = in.readLong();
        return new BoundedInputStream(in, size);
    }
}
