package com.dervarex.minified.worlds.save;

import com.dervarex.minified.worlds.exceptions.WorldLockedException;
import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import org.apiguardian.api.API;

@Getter
@API(status = API.Status.STABLE)
public class SessionLock {
    // java only lets a JVM lock a file once, so all handles for a world share one
    private static final Map<Path, Held> HELD = new HashMap<>();

    private final boolean locked;

    public SessionLock(Path path) {
        boolean isLocked;
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            FileLock lock = channel.tryLock();
            if (lock != null) {
                isLocked = false;
                lock.release();
            } else {
                isLocked = true;
            }
        } catch (OverlappingFileLockException e) {
            isLocked = true;
        } catch (IOException e) {
            isLocked = false;
        }
        this.locked = isLocked;
    }

    /**
     * Takes the world's session.lock like Minecraft does, so the game keeps its hands off the world until the handle is closed.
     * Every write in minified-worlds takes it anyway, take it yourself if you keep a world open for longer (an editor, say).
     * Taking it twice is fine. Disk I/O, creates session.lock if it's missing
     * @param worldDirectory the folder with level.dat in it
     * @throws WorldLockedException if Minecraft (or something else) has the world open
     * @throws IOException if the lock file can't be opened
     */
    public static Handle acquire(Path worldDirectory) throws IOException {
        Path file = worldDirectory.toAbsolutePath().normalize().resolve("session.lock");
        synchronized (HELD) {
            Held held = HELD.get(file);
            if (held == null) {
                FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock lock;
                try {
                    lock = channel.tryLock();
                } catch (OverlappingFileLockException e) {
                    channel.close();
                    throw new WorldLockedException(file + " is already locked by this program", file, e);
                } catch (IOException e) {
                    channel.close();
                    throw e;
                }
                if (lock == null) {
                    channel.close();
                    throw new WorldLockedException("The world is open somewhere else, probably in Minecraft (" + file + " is locked)", file, null);
                }
                held = new Held(channel, lock);
                HELD.put(file, held);
            }
            held.handles++;
            return new Handle(file);
        }
    }

    /**
     * Holds the lock until you close it, see {@link #acquire(Path)}
     */
    public static final class Handle implements Closeable {
        private final Path file;
        private boolean closed;

        private Handle(Path file) {
            this.file = file;
        }

        /**
         * Gives the lock back, closing twice doesn't hurt
         * @throws IOException if releasing the lock fails
         */
        @Override
        public void close() throws IOException {
            synchronized (HELD) {
                if (closed) return;
                closed = true;
                Held held = HELD.get(file);
                if (--held.handles > 0) return;
                HELD.remove(file);
                try {
                    held.lock.release();
                } finally {
                    held.channel.close();
                }
            }
        }
    }

    private static final class Held {
        private final FileChannel channel;
        private final FileLock lock;
        private int handles;

        private Held(FileChannel channel, FileLock lock) {
            this.channel = channel;
            this.lock = lock;
        }
    }
}