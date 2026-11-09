package com.dervarex.minified.launch.launch;

import com.dervarex.minified.events.EventBus;
import com.dervarex.minified.launch.launch.modding.Loader;
import lombok.AccessLevel;
import lombok.Getter;
import org.apiguardian.api.API;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for Launching the Game. <br>
 * Example Usage:
 * <pre>
 * {@code
 * LaunchConfiguration config = new LaunchConfiguration.Builder()
 *         .downloadThreads(10)
 *         .launcherName("YourLauncherName")
 *         .launcherVersion("1.0.0")
 *         .assetsDirectory(Path.of("<assets-directory>"))
 *         .librariesDirectory(Path.of("<libraries-directory>"))
 *         .jarFile(Path.of("<client.jar>"))
 *         .isDemoUser(false)
 *         .loader(new VanillaLoader("1.21.11"))
 *         .build();
 * }
 * </pre>
 */
@Getter
@API(status = API.Status.STABLE)
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
    private boolean fullscreen = false;

    // Paths
    private Path jarFile;            // required
    private Path librariesDirectory; // required
    private Path assetsDirectory;    // required
    private Path nativesDirectory;   // optional, defaults to <jarFile's parent directory>/natives
    private Path gameDirectory;      // optional, defaults to <jarFile's parent directory>
    private Path customJavaExecutable;

    // Launch options
    @Getter(AccessLevel.NONE)
    private final List<String> extraJvmArgs = new ArrayList<>();
    @Getter(AccessLevel.NONE)
    private final List<String> extraGameArgs = new ArrayList<>();
    @Getter(AccessLevel.NONE)
    private final Map<String, String> environmentVariables = new LinkedHashMap<>();
    private String serverAddress = null; // host or host:port, joined right after the game started
    private Loader loader = null;

    // Game output
    private boolean captureGameOutput = false;

    // Headless test options
    private boolean headless = false;
    private int headlessTimeoutSeconds = 180;
    private String headlessMarker = null;

    // User
    private String offlineUsername = "Player";

    // Events
    private EventBus eventBus;

    private LaunchConfiguration(EventBus eventBus) {
        this.eventBus = eventBus != null ? eventBus : new EventBus();
    }

    /**
     * {@return the extra JVM args, read-only}
     */
    public List<String> getExtraJvmArgs() {
        return Collections.unmodifiableList(extraJvmArgs);
    }

    /**
     * {@return the extra game args, read-only}
     */
    public List<String> getExtraGameArgs() {
        return Collections.unmodifiableList(extraGameArgs);
    }

    /**
     * {@return the environment variables for the game process, read-only}
     */
    public Map<String, String> getEnvironmentVariables() {
        return Collections.unmodifiableMap(environmentVariables);
    }

    /**
     * Resolves the directory native libraries are extracted to and loaded from
     *
     * @return the configured natives directory, or {@code <jarFile's parent directory>/natives} if none was set
     */
    public Path resolveNativesDirectory() {
        if (nativesDirectory != null) {
            return nativesDirectory.toAbsolutePath();
        }
        return jarFile.toAbsolutePath().getParent().resolve("natives");
    }

    /**
     * Resolves the directory the game runs in, where saves, mods, options and logs end up
     *
     * @return the configured game directory, or {@code <jarFile's parent directory>} if none was set
     */
    public Path resolveGameDirectory() {
        if (gameDirectory != null) {
            return gameDirectory.toAbsolutePath();
        }
        return jarFile.toAbsolutePath().getParent();
    }

    /**
     * Builds a {@link LaunchConfiguration}, see the class docs for an example
     */
    public static class Builder {

        private final LaunchConfiguration config;
        private EventBus eventBus = new EventBus();

        /**
         * Creates a builder with the defaults
         */
        public Builder() {
            this.config = new LaunchConfiguration(eventBus);
        }

        // Memory
        /**
         * Sets the minimum RAM, 2048 by default
         *
         * @param minRam the minimum RAM in MB
         * @return this builder
         */
        public Builder minRam(int minRam) {
            config.minRam = minRam;
            return this;
        }

        /**
         * Sets the maximum RAM, 4096 by default
         *
         * @param maxRam the maximum RAM in MB
         * @return this builder
         */
        public Builder maxRam(int maxRam) {
            config.maxRam = maxRam;
            return this;
        }

        // Downloads
        /**
         * Sets how many files get downloaded at once, 5 by default
         *
         * @param threads the amount of download threads
         * @return this builder
         */
        public Builder downloadThreads(int threads) {
            config.downloadThreads = threads;
            return this;
        }

        // Resolution
        /**
         * Sets the window size, without it the game picks its own
         *
         * @param width the width in pixels
         * @param height the height in pixels
         * @return this builder
         */
        public Builder resolution(int width, int height) {
            config.resolutionWidth = width;
            config.resolutionHeight = height;
            config.customResolution = true;
            return this;
        }

        // Launcher metadata
        /**
         * Sets the launcher name the game gets told about
         *
         * @param name the launcher name
         * @return this builder
         */
        public Builder launcherName(String name) {
            config.launcherName = name;
            return this;
        }

        /**
         * Sets the launcher version the game gets told about
         *
         * @param version the launcher version
         * @return this builder
         */
        public Builder launcherVersion(String version) {
            config.launcherVersion = version;
            return this;
        }

        // Flags
        /**
         * Launches the demo, false by default
         *
         * @param demo whether to launch the demo
         * @return this builder
         */
        public Builder isDemoUser(boolean demo) {
            config.demoUser = demo;
            return this;
        }

        /**
         * Starts the game in fullscreen, false by default
         *
         * @param fullscreen whether to start in fullscreen
         * @return this builder
         */
        public Builder fullscreen(boolean fullscreen) {
            config.fullscreen = fullscreen;
            return this;
        }

        // Paths
        /**
         * Sets where the client jar goes, required
         *
         * @param jarFile the path to the client jar
         * @return this builder
         */
        public Builder jarFile(Path jarFile) {
            config.jarFile = jarFile;
            return this;
        }

        /**
         * Sets where the libraries go, required
         *
         * @param librariesDirectory the libraries directory
         * @return this builder
         */
        public Builder librariesDirectory(Path librariesDirectory) {
            config.librariesDirectory = librariesDirectory;
            return this;
        }

        /**
         * Sets where the assets go, required
         *
         * @param assetsDirectory the assets directory
         * @return this builder
         */
        public Builder assetsDirectory(Path assetsDirectory) {
            config.assetsDirectory = assetsDirectory;
            return this;
        }

        /**
         * Sets where native libraries get extracted to
         *
         * @param nativesDirectory the natives directory, defaults to {@code <jarFile's parent directory>/natives}
         * @return this builder
         */
        public Builder nativesDirectory(Path nativesDirectory) {
            config.nativesDirectory = nativesDirectory;
            return this;
        }

        /**
         * Lets the game run somewhere else than next to its jar, so several instances can share one client jar
         * and the loader files installed next to it.
         *
         * @param gameDirectory where saves, mods, options and logs go, defaults to the jar's parent directory
         * @return this builder
         */
        public Builder gameDirectory(Path gameDirectory) {
            config.gameDirectory = gameDirectory;
            return this;
        }

        /**
         * Uses your own Java instead of the one we download
         *
         * @param customJavaExecutable the path to the java executable
         * @return this builder
         */
        public Builder customJavaExecutable(Path customJavaExecutable) {
            config.customJavaExecutable = customJavaExecutable;
            return this;
        }

        // Launch options
        /**
         * Adds a JVM arg
         *
         * @param arg the arg, like {@code -XX:+UseG1GC}
         * @return this builder
         */
        public Builder extraJvmArg(String arg) {
            config.extraJvmArgs.add(arg);
            return this;
        }

        /**
         * Adds JVM args, null is ignored
         *
         * @param args the args
         * @return this builder
         */
        public Builder extraJvmArgs(List<String> args) {
            if (args != null) {
                config.extraJvmArgs.addAll(args);
            }
            return this;
        }

        /**
         * Adds an arg to the end of the game args
         *
         * @param arg the arg, for example {@code --demo}
         * @return this builder
         */
        public Builder extraGameArg(String arg) {
            config.extraGameArgs.add(arg);
            return this;
        }

        /**
         * Adds args to the end of the game args, null is ignored
         *
         * @param args the args
         * @return this builder
         */
        public Builder extraGameArgs(List<String> args) {
            if (args != null) {
                config.extraGameArgs.addAll(args);
            }
            return this;
        }

        /**
         * Sets an environment variable for the game process, it wins over the launcher's own environment
         *
         * @param name the variable name
         * @param value the value
         * @return this builder
         */
        public Builder environmentVariable(String name, String value) {
            config.environmentVariables.put(name, value);
            return this;
        }

        /**
         * Sets environment variables for the game process, null is ignored
         *
         * @param variables the variables
         * @return this builder
         */
        public Builder environmentVariables(Map<String, String> variables) {
            if (variables != null) {
                config.environmentVariables.putAll(variables);
            }
            return this;
        }

        /**
         * Joins a multiplayer server as soon as the game has started
         * Uses quick play on 1.20 and newer, {@code --server}/{@code --port} on older versions, does not work before 1.6
         *
         * @param serverAddress {@code host} or {@code host:port}, null to disable
         * @return this builder
         */
        public Builder serverAddress(String serverAddress) {
            config.serverAddress = serverAddress;
            return this;
        }

        /**
         * Sets the loader and Minecraft version, required
         *
         * @param loader the loader, like {@code new VanillaLoader("1.21.11")}
         * @return this builder
         */
        public Builder loader(Loader loader) {
            config.loader = loader;
            return this;
        }

        // Game output

        /**
         * Reads the game's stdout and stderr instead of handing them to the launcher's console, every line
         * gets posted as a {@code GameOutputEvent}
         *
         * @param captureGameOutput whether to capture the output, false by default
         * @return this builder
         */
        public Builder captureGameOutput(boolean captureGameOutput) {
            config.captureGameOutput = captureGameOutput;
            return this;
        }

        // Headless test options
        /**
         * Stops the game once it reached the main menu, used for tests
         *
         * @param headless whether to run headless, false by default
         * @return this builder
         */
        public Builder headless(boolean headless) {
            config.headless = headless;
            return this;
        }

        /**
         * Sets how long a headless game gets to reach the main menu, 180 by default
         *
         * @param headlessTimeoutSeconds the timeout in seconds
         * @return this builder
         */
        public Builder headlessTimeoutSeconds(int headlessTimeoutSeconds) {
            config.headlessTimeoutSeconds = headlessTimeoutSeconds;
            return this;
        }

        /**
         * Sets the log lines that count as reaching the main menu in headless mode
         *
         * @param headlessMarker comma separated parts of log lines, null for the defaults
         * @return this builder
         */
        public Builder headlessMarker(String headlessMarker) {
            config.headlessMarker = headlessMarker;
            return this;
        }

        // User
        /**
         * Only uses the custom username if offline mode is enabled.
         *
         * @param username the username to use
         * @return this builder
         */
        public Builder offlineUsername(String username) {
            config.offlineUsername = username;
            return this;
        }

        // Events
        /**
         * Sets the event bus everything gets posted to
         *
         * @param eventBus the event bus, null creates a new one
         * @return this builder
         */
        public Builder eventBus(EventBus eventBus) {
            this.eventBus = eventBus != null ? eventBus : new EventBus();
            return this;
        }

        /**
         * {@return the launch configuration}
         *
         * @throws NullPointerException if jarFile, librariesDirectory, assetsDirectory or loader is missing
         */
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