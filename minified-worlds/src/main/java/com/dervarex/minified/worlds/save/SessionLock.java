package com.dervarex.minified.worlds.save;

import lombok.Getter;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Getter
public class SessionLock {
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
}