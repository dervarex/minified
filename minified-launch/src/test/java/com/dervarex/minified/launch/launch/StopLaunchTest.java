package com.dervarex.minified.launch.launch;

import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.events.type.connection.CheckConnectionEvent;
import com.dervarex.minified.launch.TestEnvironment;
import com.dervarex.minified.launch.events.control.StopGameEvent;
import com.dervarex.minified.launch.events.download.libraries.DownloadLibrariesEvent;
import com.dervarex.minified.launch.events.launch.GameStartEvent;
import com.dervarex.minified.launch.events.launch.LaunchCancelledEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StopLaunchTest {
    @TempDir
    Path tempDir;

    @Test
    void stopBeforeDownloading() {
        LaunchConfiguration config = TestEnvironment.config(tempDir);
        EventBus eventBus = config.getEventBus();
        AtomicInteger cancelled = new AtomicInteger();
        AtomicBoolean downloaded = new AtomicBoolean();
        eventBus.subscribe(LaunchCancelledEvent.class, event -> cancelled.incrementAndGet());
        eventBus.subscribe(DownloadLibrariesEvent.class, event -> downloaded.set(true));
        eventBus.subscribe(CheckConnectionEvent.class, event -> eventBus.post(new StopGameEvent()));

        Launcher.launchMinecraft(null, config);

        assertEquals(1, cancelled.get());
        assertFalse(downloaded.get());
        assertFalse(Thread.currentThread().isInterrupted());
    }

    @Test
    void stopWhileDownloadingLibraries() throws IOException {
        LaunchConfiguration config = TestEnvironment.config(tempDir);
        EventBus eventBus = config.getEventBus();
        AtomicInteger cancelled = new AtomicInteger();
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean stopPosted = new AtomicBoolean();
        eventBus.subscribe(LaunchCancelledEvent.class, event -> cancelled.incrementAndGet());
        eventBus.subscribe(GameStartEvent.class, event -> started.set(true));
        eventBus.subscribe(DownloadLibrariesEvent.class, event -> {
            if (event.progress() > 0.1 && stopPosted.compareAndSet(false, true)) {
                eventBus.post(new StopGameEvent());
            }
        });

        long start = System.nanoTime();
        Launcher.launchMinecraft(null, config);
        long seconds = (System.nanoTime() - start) / 1_000_000_000L;

        assertTrue(stopPosted.get(), "the libraries were never downloaded");
        assertEquals(1, cancelled.get());
        assertFalse(started.get());
        assertFalse(Thread.currentThread().isInterrupted());
        assertFalse(Files.exists(config.getAssetsDirectory().resolve("objects")), "kept downloading assets after the stop");
        try (Stream<Path> files = Files.walk(config.getLibrariesDirectory())) {
            assertEquals(0, files.filter(p -> p.toString().endsWith(".tmp")).count(), "left half downloaded files behind");
        }
        assertTrue(seconds < 60, "took " + seconds + "s to stop");
    }
}
