package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FileInfo {
    @NotNull
    public final String name;

    public final boolean isDirectory;

    public FileInfo(@NotNull final String name, final boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    @NotNull
    public static FileInfo read(@NotNull final DataInputStream in) throws IOException {
        final String name = in.readUTF();
        final boolean isDirectory = in.readBoolean();
        return new FileInfo(name, isDirectory);
    }

    public void write(@NotNull final DataOutputStream out) throws IOException {
        out.writeUTF(name);
        out.writeBoolean(isDirectory);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final FileInfo fileInfo = (FileInfo) object;
        return isDirectory == fileInfo.isDirectory && name.equals(fileInfo.name);
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + (isDirectory ? 1 : 0);
    }

    @Override
    public String toString() {
        return "FileInfo{name='" + name + "', isDirectory=" + isDirectory + "}";
    }
}
