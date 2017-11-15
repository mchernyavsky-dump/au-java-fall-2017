package ru.spbau.mit;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.spbau.mit.client.FTPClient;
import ru.spbau.mit.server.FTPServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestClientServerIntegration {
    @NotNull
    private static final String HOST = "localhost";

    private static final int PORT = 4242;

    @NotNull
    private static final Path ROOT = Paths.get("src/test/resources/testRoot");

    private static final int TEST_TIMEOUT = 100;

    @Nullable
    private FTPClient client;
    @Nullable
    private FTPServer server;

    @Before
    public void setUp() throws IOException {
        server = new FTPServer(PORT, ROOT);
        server.start();
        client = new FTPClient();
        client.connect(HOST, PORT);
    }

    @After
    public void tearDown() throws InterruptedException {
        if (client != null) {
            try {
                client.disconnect();
            } catch (IOException ignored) {
            } finally {
                client = null;
            }
        }

        if (server != null) {
            try {
                server.stop();
            } catch (IOException ignored) {
            } finally {
                server = null;
            }
        }

        Thread.sleep(TEST_TIMEOUT);
    }

    @Test
    public void testListRequestSimple() throws IOException {
        final List<FileInfo> expected = new ArrayList<FileInfo>() {{
            add(new FileInfo("file1.txt", false));
            add(new FileInfo("file2.txt", false));
            add(new FileInfo("dir1", true));
            add(new FileInfo("dir2", true));
        }};
        final List<FileInfo> actual = client.executeList(".");
        assertEquals(expected, actual);
    }

    @Test
    public void testListRequestEmpty() throws IOException {
        final List<FileInfo> expected = Collections.emptyList();
        final List<FileInfo> actual = client.executeList("dir2");
        assertEquals(expected, actual);
    }

    @Test
    public void testListRequestNonExistent() throws IOException {
        final List<FileInfo> expected = Collections.emptyList();
        final List<FileInfo> actual = client.executeList("dir0");
        assertEquals(expected, actual);
    }

    @Test
    public void testListRequestFile() throws IOException {
        final List<FileInfo> expected = Collections.emptyList();
        final List<FileInfo> actual = client.executeList("file1.txt");
        assertEquals(expected, actual);
    }

    @Test
    public void testGetRequestSimple() throws IOException {
        final String fileName = "file1.txt";
        final byte[] expected = Files.readAllBytes(ROOT.resolve(fileName));
        final byte[] actual = IOUtils.toByteArray(client.executeGet(fileName));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetRequestEmpty() throws IOException {
        final String fileName = "file2.txt";
        final byte[] expected = Files.readAllBytes(ROOT.resolve(fileName));
        final byte[] actual = IOUtils.toByteArray(client.executeGet(fileName));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetRequestNonExistent() throws IOException {
        final String fileName = "file0.txt";
        final byte[] expected = new byte[0];
        final byte[] actual = IOUtils.toByteArray(client.executeGet(fileName));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetRequestDirectory() throws IOException {
        final String fileName = "dir1";
        final byte[] expected = new byte[0];
        final byte[] actual = IOUtils.toByteArray(client.executeGet(fileName));
        assertArrayEquals(expected, actual);
    }
}
