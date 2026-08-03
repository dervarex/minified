package com.dervarex.minified.launch.launch;

import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.launch.launch.modding.Loader;
import lombok.Getter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@SuppressWarnings("unused")
public class LaunchConfiguration {
    // Memory
    private int minRam = 2048;
    private int maxRam = 4096;

    // Downloads
    private int downloadThreads = 5;

    // Resolution
    private int resolutionWidth = 1920;
    private int resolutionHeight = 1080;

    // Launcher metadata
    private String launcherName = "Launcher";
    private String launcherVersion = "1.0.0";

    // Flags
    private boolean demoUser = false;
    private boolean customResolution = false;

    // Paths
    private Path jarFile;            // required
    private Path librariesDirectory; // required
    private Path assetsDirectory;    // required
    private Path nativesDirectory;   // optional, defaults to <jarFile's parent directory>/natives
    private Path customJavaExecutable;

    // Launch options
    private final List<String> extraJvmArgs = new ArrayList<>();
    private Loader loader = null;

    // User
    private String offlineUsername = "Player";

    // Events
    private EventBus eventBus;

    private LaunchConfiguration(EventBus eventBus) {
        this.eventBus = eventBus != null ? eventBus : new EventBus();
    }

    public static class Builder {

        private final LaunchConfiguration config;
        private EventBus eventBus = new EventBus();

        public Builder() {
            this.config = new LaunchConfiguration(eventBus);
        }

        // Memory
        public Builder minRam(int minRam) {
            config.minRam = minRam;
            return this;
        }

        public Builder maxRam(int maxRam) {
            config.maxRam = maxRam;
            return this;
        }

        // Downloads
        public Builder downloadThreads(int threads) {
            config.downloadThreads = threads;
            return this;
        }

        // Resolution
        public Builder resolution(int width, int height) {
            config.resolutionWidth = width;
            config.resolutionHeight = height;
            config.customResolution = true;
            return this;
        }

        // Launcher metadata
        public Builder launcherName(String name) {
            config.launcherName = name;
            return this;
        }

        public Builder launcherVersion(String version) {
            config.launcherVersion = version;
            return this;
        }

        // Flags
        public Builder isDemoUser(boolean demo) {
            config.demoUser = demo;
            return this;
        }

        // Paths
        public Builder jarFile(Path jarFile) {
            config.jarFile = jarFile;
            return this;
        }

        public Builder librariesDirectory(Path librariesDirectory) {
            config.librariesDirectory = librariesDirectory;
            return this;
        }

        public Builder assetsDirectory(Path assetsDirectory) {
            config.assetsDirectory = assetsDirectory;
            return this;
        }

        public Builder nativesDirectory(Path nativesDirectory) {
            config.nativesDirectory = nativesDirectory;
            return this;
        }

        public Builder customJavaExecutable(Path customJavaExecutable) {
            config.customJavaExecutable = customJavaExecutable;
            return this;
        }

        // Launch options
        public Builder extraJvmArg(String arg) {
            config.extraJvmArgs.add(arg);
            return this;
        }

        public Builder extraJvmArgs(List<String> args) {
            config.extraJvmArgs.addAll(args);
            return this;
        }

        public Builder loader(Loader loader) {
            config.loader = loader;
            return this;
        }

        // User
        /**
         * Only uses the custom username if offline mode is enabled.
         *
         * @param username the username to use
         */
        public Builder offlineUsername(String username) {
            config.offlineUsername = username;
            return this;
        }

        // Events
        public Builder eventBus(EventBus eventBus) {
            this.eventBus = eventBus != null ? eventBus : new EventBus();
            return this;
        }

        public LaunchConfiguration build() {
            config.eventBus = eventBus;

            Objects.requireNonNull(config.jarFile, "jarFile is required");
            Objects.requireNonNull(config.librariesDirectory, "librariesDirectory is required");
            Objects.requireNonNull(config.assetsDirectory, "assetsDirectory is required");
            Objects.requireNonNull(config.loader, "loader is required");

            return config;
        }
    }
}
