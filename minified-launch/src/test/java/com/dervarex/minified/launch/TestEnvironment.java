package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.LaunchConfigurator;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;

import java.nio.file.Path;

public final class TestEnvironment {

    private static final Path BASE_DIR = Path.of("/home/dervarex/Development/tmp");

    private static final Path AUTH_DIR = BASE_DIR.resolve("login");
    private static final Path ASSETS_DIR = BASE_DIR.resolve("assets");
    private static final Path LIBRARIES_DIR = BASE_DIR.resolve("jar/libraries");
    private static final Path CLIENT_JAR = BASE_DIR.resolve("jar/client.jar");

    private static final LaunchConfigurator CONFIG = new LaunchConfigurator.Builder()
            .downloadThreads(10)
            .launcherName("MinifiedLauncher")
            .launcherVersion("1.0.0")
            .assetsDirectory(ASSETS_DIR)
            .librariesDirectory(LIBRARIES_DIR)
            .jarFile(CLIENT_JAR)
            .isDemoUser(false)
            .loader(new FabricLoader("1.21.11", "0.16.14"))
            .build();

    private TestEnvironment() {
    }

    public static Path authDirectory() {
        return AUTH_DIR;
    }

    public static LaunchConfigurator config() {
        return CONFIG;
    }
}