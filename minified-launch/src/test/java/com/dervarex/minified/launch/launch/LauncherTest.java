package com.dervarex.minified.launch.launch;

import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.launch.TestEnvironment;
import com.dervarex.minified.launch.events.download.assets.DownloadAssetsEvent;
import com.dervarex.minified.launch.events.control.StopGameEvent;
import com.dervarex.minified.launch.events.download.client.DownloadClientJarEvent;
import com.dervarex.minified.launch.events.launch.GameProcessStartedEvent;
import com.dervarex.minified.launch.events.launch.GameStoppedEvent;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Tag("manual")
class LauncherTest {
    @TempDir
    Path tempDir;

    @Test
    void launch() {
        Launcher.launchMinecraft(
                null,
                TestEnvironment.config(tempDir)
        );
    }

    @Test
    void launchWithProgressOutput() {
        EventBus eventBus = new EventBus();
        LaunchConfiguration launchConfiguration = new LaunchConfiguration.Builder()
                .downloadThreads(10)
                .launcherName("MinifiedLauncher")
                .launcherVersion("1.0.0")
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("jar/libraries"))
                .jarFile(tempDir.resolve("jar/client.jar"))
                .isDemoUser(false)
                .loader(new FabricLoader("1.21.11", "0.16.14"))
                .eventBus(eventBus)
                .build();

        eventBus.subscribe(DownloadAssetsEvent.class, event -> {
            System.out.printf(
                    "\rDownloading assets | %.2f%% | %s | %d/%d bytes",
                    event.progress() * 100,
                    event.currentFile(),
                    event.downloadedBytes(),
                    event.totalBytes()
            );
            System.out.flush();
        });
        // we will not print the libs to the terminal, when I tried this I ended up creating an own terminal renderer... it should work in a gui though

        eventBus.subscribe(DownloadClientJarEvent.class, event -> {
            System.out.printf("\rDownloading client.jar | %.2f%%", event.progress() * 100);
            System.out.flush();
        });
        Launcher.launchMinecraft(
                null,
                launchConfiguration
        );
    }

    @Test
    void stopGame() {
        LaunchConfiguration config = TestEnvironment.config(tempDir);
        EventBus eventBus = config.getEventBus();
        AtomicInteger exitCode = new AtomicInteger(Integer.MIN_VALUE);
        eventBus.subscribe(GameStoppedEvent.class, event -> exitCode.set(event.exitCode()));
        // a few secs after game launch, we're gonna stop the game
        eventBus.subscribe(GameProcessStartedEvent.class, event -> CompletableFuture.runAsync(
                () -> eventBus.post(new StopGameEvent()),
                CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
        ));

        // would throw a NonZeroExitCodeException if the stop wasn't recognized as requested
        Launcher.launchMinecraft(null, config);

        assertNotEquals(Integer.MIN_VALUE, exitCode.get());
    }
}
