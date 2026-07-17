package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;

import java.nio.file.Path;

public final class TestEnvironment {

    private TestEnvironment() {
    }

    public static LaunchConfiguration config(Path tempDir) {
        return new LaunchConfiguration.Builder()
                .downloadThreads(10)
                .launcherName("MinifiedLauncher")
                .launcherVersion("1.0.0")
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("jar/libraries"))
                .jarFile(tempDir.resolve("jar/client.jar"))
                .isDemoUser(false)
                .loader(new FabricLoader("1.21.11", "0.16.14"))
                .build();
    }
}