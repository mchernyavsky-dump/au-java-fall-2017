package ru.spbau.mit.server;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.FTPConnection;
import ru.spbau.mit.FileInfo;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class FTPServerConnection extends FTPConnection {

    public FTPServerConnection(@NotNull final Socket socket) throws IOException {
        super(socket);
    }

    public int receiveRequestType() throws IOException {
        return in.readInt();
    }

    @NotNull
    public String receiveListRequestBody() throws IOException {
        return in.readUTF();
    }

    @NotNull
    public String receiveGetRequestBody() throws IOException {
        return in.readUTF();
    }

    public void sendListResponse(@NotNull final List<FileInfo> fileInfos) throws IOException {
        out.writeInt(fileInfos.size());
        for (final FileInfo info : fileInfos) {
            info.write(out);
        }
        out.flush();
    }

    public void sendGetResponse(@NotNull final File file) throws IOException {
        out.writeLong(file.length());
        FileUtils.copyFile(file, out);
        out.flush();
    }
}
