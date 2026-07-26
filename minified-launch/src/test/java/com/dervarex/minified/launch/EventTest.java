package com.dervarex.minified.launch;

import com.dervarex.minified.launch.events.EventBus;
import com.dervarex.minified.launch.events.type.download.assets.DownloadAssetsEvent;
import com.dervarex.minified.launch.events.type.download.client.DownloadClientJarEvent;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.Launcher;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

@Tag("manual")
public class EventTest {
    @TempDir
    public Path tempDir;

    @Test
    void launch() {
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
}