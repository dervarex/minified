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

    @Test
    void acquireKeepsEveryoneElseOut(@TempDir Path world) throws IOException {
        Path lockFile = world.resolve("session.lock");

        try (SessionLock.Handle handle = SessionLock.acquire(world)) {
            assertTrue(new SessionLock(lockFile).isLocked());
        }
        assertFalse(new SessionLock(lockFile).isLocked());
    }

    @Test
    void acquireCanBeNested(@TempDir Path world) throws IOException {
        Path lockFile = world.resolve("session.lock");
        SessionLock.Handle outer = SessionLock.acquire(world);
        SessionLock.Handle inner = SessionLock.acquire(world);

        inner.close();
        inner.close(); // closing twice must not steal the outer one's lock
        assertTrue(new SessionLock(lockFile).isLocked());

        outer.close();
        assertFalse(new SessionLock(lockFile).isLocked());
    }

    @Test
    void acquireFailsWhileTheGameHasTheWorld(@TempDir Path world) throws IOException {
        Path lockFile = world.resolve("session.lock");
        Files.write(lockFile, new byte[0]);

        try (FileChannel game = FileChannel.open(lockFile, StandardOpenOption.WRITE); FileLock held = game.lock()) {
            assertThrows(IOException.class, () -> SessionLock.acquire(world));
        }
        SessionLock.acquire(world).close();
    }
}
