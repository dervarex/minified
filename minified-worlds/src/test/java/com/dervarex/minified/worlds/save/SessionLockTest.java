package com.dervarex.minified.worlds.save;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

class SessionLockTest {

    @Test
    void testIsLockedReturnsFalse(@TempDir Path dir) throws IOException {
        Path lockFile = dir.resolve("session.lock");
        Files.write(lockFile, new byte[0]);

        SessionLock lock = new SessionLock(lockFile);

        assertFalse(lock.isLocked());
    }

    @Test
    void testIsLockedReturnsTrue(@TempDir Path dir) throws IOException {
        Path lockFile = dir.resolve("session.lock");
        Files.write(lockFile, new byte[0]);

        try (FileChannel holder = FileChannel.open(lockFile, StandardOpenOption.WRITE)) {
            FileLock heldLock = holder.lock();
            try {
                SessionLock lock = new SessionLock(lockFile);
                assertTrue(lock.isLocked());
            } finally {
                heldLock.release();
            }
        }
    }

    @Test
    void testIsLockedWithMissingFile(@TempDir Path dir) {
        Path missing = dir.resolve("does-not-exist.lock");
        SessionLock lock = new SessionLock(missing);
        assertFalse(lock.isLocked());
    }
}
