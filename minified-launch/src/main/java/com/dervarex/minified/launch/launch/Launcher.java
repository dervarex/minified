package com.dervarex.minified.launch.launch;

import com.dervarex.minified.auth.user.User;
import com.dervarex.minified.events.type.connection.CheckConnectionEvent;
import com.dervarex.minified.events.type.connection.OfflineEvent;
import com.dervarex.minified.java.JavaInstallation;
import com.dervarex.minified.java.JavaManager;
import com.dervarex.minified.launch.download.ClientDownloader;
import com.dervarex.minified.launch.download.assets.AssetDownloader;
import com.dervarex.minified.launch.download.assets.LegacyAssets;
import com.dervarex.minified.launch.download.libraries.LibraryDownloader;
import com.dervarex.minified.launch.events.launch.GameStartEvent;
import com.dervarex.minified.launch.events.launch.GameStoppedEvent;
import com.dervarex.minified.launch.exceptions.loader.UnexpectedLoaderException;
import com.dervarex.minified.launch.exceptions.version.MalformedVersionJsonException;
import com.dervarex.minified.launch.launch.internal.ArgumentsBuilder;
import com.dervarex.minified.launch.launch.internal.CacheManager;
import com.dervarex.minified.launch.launch.internal.ClasspathBuilder;
import com.dervarex.minified.launch.launch.internal.LaunchOptions;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.custom.CustomLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricProfileJsonLoader;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeProfileJsonLoader;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionJson;
import com.dervarex.minified.launch.launch.modding.forge.installer.ForgeInstallerInjector;
import com.dervarex.minified.launch.launch.modding.forge.installer.LegacyForgeGameJar;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoProfileJsonLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.installer.NeoInstallerInjector;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltProfileJsonLoader;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.launch.utils.X11Helper;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.network.NetworkUtil;
import org.apiguardian.api.API;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Launcher {
    /**
     * Downloads required files and launches Minecraft.
     * Can be used in offline mode, will throw {@code OfflineModeNeedsNetworkException} if any assets, libraries or other stuff is not downloaded but needed
     *
     * @param user the logged-in user to launch with, or null to launch in offline mode(you won't be able to join online servers or use any online features in offline mode)
     * @param launchConfig configuration used by the launcher when starting the game,
     * such as the number of download threads, launcher name,
     * and launcher version
     *<p>
     * Example:
     * <pre>{@code
     * LaunchConfiguration config = new LaunchConfiguration.Builder()
     * .downloadThreads(10)
     * .launcherName("MinifiedLauncher")
     * .launcherVersion("1.0.0")
     * .assetsDirectory(Path.of("path to assets directory"))
     * .librariesDirectory(Path.of("path to library directory"))
     * .jarFile(Path.of("path to client.jar"))
     * .build();
     * }</pre>
     */
    @API(status = API.Status.STABLE)
    public static void launchMinecraft(
            User user,
            LaunchConfiguration launchConfig) {
        LaunchContext context = new LaunchContext(user, launchConfig);

        try {
            Loader loader = launchConfig.getLoader();

            context.setOnline(true);
            try {
                context.getEventBus().post(new CheckConnectionEvent());
                NetworkUtil.ensureOnline("launch Minecraft");
            } catch (NoConnectionException e) {
                context.setOnline(false);
                context.getEventBus().post(new OfflineEvent());
            }

            if (context.isOnline()) {
                if (loader instanceof ForgeLoader) {
                    ForgeInstallerInjector forgeInstallerInjector = new ForgeInstallerInjector();
                    forgeInstallerInjector.install(context);
                } else if (loader instanceof NeoforgeLoader) {
                    NeoInstallerInjector neoInstallerInjector = new NeoInstallerInjector();
                    neoInstallerInjector.install(context);
                }
            }

            JsonFile versionJson = CacheManager.loadVersionJson(loader.mcVersion(), context.isOnline());

            JavaInstallation javaInstallation;
            if (launchConfig.getCustomJavaExecutable() == null) {
                int requiredJavaVersion = JavaManager.getRequiredJavaVersion(versionJson);
                if (requiredJavaVersion <= 0 && versionJson.get("arguments") == null) {
                    // the 1.6.x version JSONs have no javaVersion, everything with legacy arguments wants Java 8,
                    // if you give it java 21 it cries
                    requiredJavaVersion = 8;
                }
                javaInstallation = requiredJavaVersion == 8
                        ? JavaManager.ensureExactJavaVersion(requiredJavaVersion)
                        : JavaManager.ensureJavaVersion(requiredJavaVersion);
            } else {
                javaInstallation = null;
            }

            downloadFiles(
                    loader.mcVersion(),
                    context
            );
            LegacyAssets.reconstruct(versionJson, launchConfig);
            if (loader instanceof ForgeLoader) {
                LegacyForgeGameJar.prepare(
                        ForgeVersionJson.getVersionJson(launchConfig.getJarFile().getParent(), loader.loaderVersion()).asObject(),
                        launchConfig
                );
            }

            String classpath =
                    ClasspathBuilder.buildClasspath(
                            versionJson,
                            launchConfig,
                            context.isOnline()
                    );
            if (loader instanceof CustomLoader customLoader && customLoader.customClasspathEntries() != null) {
                StringBuilder cpBuilder = new StringBuilder(classpath);
                for (String entry : customLoader.customClasspathEntries()) {
                    if (!cpBuilder.isEmpty()) {
                        cpBuilder.append(File.pathSeparator);
                    }
                    cpBuilder.append(entry);
                }
                classpath = cpBuilder.toString();
            }

            LaunchOptions options =
                    LaunchOptions.buildLaunchOptions(
                            user,
                            loader.mcVersion(),
                            launchConfig,
                            versionJson,
                            classpath
                    );

            List<String> jvmArgs =
                    ArgumentsBuilder.buildJvmArguments(
                            versionJson,
                            launchConfig,
                            options,
                            loader,
                            loader.mcVersion(),
                            context.isOnline()
                    ); // includes the classpath
            Path nativesDir = launchConfig.resolveNativesDirectory();

            jvmArgs.add(
                    "-Djava.library.path=" + nativesDir
            );

            jvmArgs.add(
                    "-Dorg.lwjgl.librarypath=" + nativesDir
            );

            boolean logsNoMarkers = isPreOneSix(versionJson);
            if (logsNoMarkers) {
                // versions before 1.6 find their folder through user.home (~/.minecraft), LaunchWrapper only redirects
                // part of it to the game directory. The rest (output-client.log, options.txt, ...) stays in the instance this way.
                // FML up to 1.5.2 reads the old applet launcher's property instead
                Path gameDir = launchConfig.getJarFile().toAbsolutePath().getParent();
                jvmArgs.add("-Duser.home=" + gameDir);
                jvmArgs.add("-Dminecraft.applet.TargetDirectory=" + gameDir);
            }
            if (launchConfig.isHeadless() && launchConfig.getHeadlessMarker() == null && logsNoMarkers) {
                // makes LWJGL 2 log when it creates the window, see LEGACY_HEADLESS_MARKER
                jvmArgs.add("-Dorg.lwjgl.util.Debug=true");
            }

            List<String> gameArgs =
                    ArgumentsBuilder.buildGameArguments(
                            versionJson,
                            options,
                            loader,
                            loader.mcVersion(),
                            launchConfig,
                            context.isOnline()
                    );

            ArrayList<String> command =
                    new ArrayList<>();

            command.add(
                    javaInstallation != null ?
                            javaInstallation.executable()
                                    .toAbsolutePath().toString() :
                            launchConfig.getCustomJavaExecutable()
                                    .toAbsolutePath().toString());                                                  // java
            command.addAll(jvmArgs);                                                                                // -Dsomearg -cp ...
            command.add   (getMainClass(versionJson, loader, loader.mcVersion(), launchConfig, context.isOnline()));// net.minecraft.client.main.Main
            command.addAll(gameArgs);                                                                               // --username ... --accessToken ...

            launchProcess(command, context, logsNoMarkers);

        } catch (HttpException | IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static void downloadFiles(
            String version,
            LaunchContext context
    ) throws HttpException, IOException {

        LaunchConfiguration launchConfig = context.getLaunchConfiguration();

        // Downloads (will skip files if they already exist)
        LibraryDownloader libDownloader    = new LibraryDownloader(launchConfig.getDownloadThreads());
        AssetDownloader   assetDownloader  = new AssetDownloader(launchConfig.getDownloadThreads());
        ClientDownloader  clientDownloader = new ClientDownloader();

        libDownloader.downloadLibraries(
                launchConfig.getLoader(),
                launchConfig.getLibrariesDirectory(),
                launchConfig.resolveNativesDirectory(),
                progress -> {},
                null
        );

        assetDownloader.downloadAssets(
                version,
                launchConfig.getAssetsDirectory(),
                context
        );

        if (context.isOnline()) {
            clientDownloader.downloadClient(
                    version,
                    launchConfig.getJarFile(),
                    context
            );
        } else if (!Files.exists(launchConfig.getJarFile())) {
            throw new OfflineModeNeedsNetworkException(
                    "Missing cached client jar: " + launchConfig.getJarFile(),
                    OfflineModeNeedsNetworkException.Reason.MISSING_CLIENT_JAR,
                    List.of(launchConfig.getJarFile().toString())
            );
        }
    }

    private static String getMainClass(
            JsonFile versionJson,
            Loader loader,
            String version,
            LaunchConfiguration launchConfig,
            boolean online
    ) {
        if (loader instanceof CustomLoader customLoader) {
            if (customLoader.mainClass() != null) {
                return customLoader.mainClass();
            }
            JsonValue vanillaMain = versionJson.get("mainClass");
            if (vanillaMain != null) return vanillaMain.asString();
        }

        JsonValue mainClassValue = switch (loader) {
            case VanillaLoader ignored -> versionJson.get("mainClass");
            case FabricLoader ignored -> FabricProfileJsonLoader.loadFabricProfileJson(version, launchConfig, online).get("mainClass");
            case QuiltLoader ignored -> QuiltProfileJsonLoader.loadQuiltProfileJson(version, launchConfig, online).get("mainClass");
            case ForgeLoader ignored -> ForgeProfileJsonLoader.loadForgeProfileJson(version, launchConfig, online).get("mainClass");
            case NeoforgeLoader ignored -> NeoProfileJsonLoader.loadNeoforgeProfileJson(version, launchConfig, online).get("mainClass");
            default -> throw new UnexpectedLoaderException("Unexpected loader: " + loader);
        };

        if (mainClassValue == null) {
            throw new MalformedVersionJsonException(
                    "Main class not found in version JSON"
            );
        }

        return mainClassValue.asString();
    }



    /**
     * @return true for versions before 1.6, they log next to nothing (classic and rubydung don't even a sound system,
     * like what should we use as a marker here?)
     */
    private static boolean isPreOneSix(JsonFile versionJson) {
        JsonValue assets = versionJson.get("assets");
        return assets != null && assets.isString() && assets.asString().equals("pre-1.6");
    }

    private static void launchProcess(
            List<String> command,
            LaunchContext context,
            boolean logsNoMarkers
    ) throws IOException, InterruptedException {

        LaunchConfiguration launchConfig = context.getLaunchConfiguration();

        ProcessBuilder processBuilder =
                new ProcessBuilder(command);
        // like the official launcher, the game runs in its game directory. Things like the log4j logs/ folder and rubydung's
        // level.dat use relative paths and would end up wherever the launcher was started from
        Path gameDir = launchConfig.getJarFile().toAbsolutePath().getParent();
        Files.createDirectories(gameDir);
        processBuilder.directory(gameDir.toFile());
        X11Helper.configureGraphicsEnvironment(
                processBuilder,
                context
        );

        if (launchConfig.isHeadless()) {
            processBuilder.redirectErrorStream(true);
        } else {
            processBuilder.inheritIO();
        }

        context.getEventBus().post(new GameStartEvent(
                context.getUser(),
                context.getLaunchConfiguration(),
                context.isOnline()
        ));

        Process process = processBuilder.start();

        int exitCode = launchConfig.isHeadless()
                ? waitForHeadlessMarker(process, launchConfig, logsNoMarkers)
                : process.waitFor();

        context.getEventBus().post(new GameStoppedEvent(exitCode, context.getLaunchConfiguration()));
//        if (exitCode != 0) {
//            throw new RuntimeException(
//                    "Minecraft exited with code " + exitCode
//            );
//        } todo: NonZeroExitCodeException or NonZeroExitCodeEvent?
    }

    // todo: move into headless maker class

    // only logged once the game window exists, stuff like "Backend library: LWJGL" also show up in crash reports
    // 1.13 - 1.15 don't log the resource reload, and without a sound device there is no sound engine either,
    // the texture atlases get built in every version, that seems usable
    private static final String DEFAULT_HEADLESS_MARKERS =
            "Reloading ResourceManager,Sound engine started,textures-atlas,.png-atlas";

    private static final String CRASH_REPORT_HEADER = "---- Minecraft Crash Report ----";

    // forge 1.7.10 - 1.11 print a crash report on purpose to log the computer specs ("THIS IS NOT A ERROR")
    private static final String FAKE_CRASH_REPORT_DESCRIPTION = "Description: Loading screen debug info";

    // the description comes a few lines after the header (header, joke, empty line, time, description)
    private static final int CRASH_REPORT_DESCRIPTION_LINES = 6;

    private static final List<String> HEADLESS_CRASH_MARKERS = List.of(
            CRASH_REPORT_HEADER,
            "#@!@# Game crashed!",
            "Exception in thread \"main\""
    );

    // logged by LWJGL 2 (with org.lwjgl.util.Debug) when it creates the window, the only thing we get from a pre 1.6
    private static final String LEGACY_HEADLESS_MARKER = "[LWJGL] Pixel format info";

    private static final long HEADLESS_GRACE_PERIOD_NANOS = TimeUnit.SECONDS.toNanos(3);

    // the window marker comes before the game loaded anything, so it has to survive a while after it
    private static final long LEGACY_HEADLESS_GRACE_PERIOD_NANOS = TimeUnit.SECONDS.toNanos(10);

    /**
     * Waits until the game logs one of the headless markers, then stops it
     *
     * @return 0 if a marker was reached without the game crashing, 124 on timeout, otherwise a non-zero exit code
     */
    private static int waitForHeadlessMarker(
            Process process,
            LaunchConfiguration launchConfig,
            boolean logsNoMarkers
    ) throws InterruptedException {

        boolean legacyMarker = launchConfig.getHeadlessMarker() == null && logsNoMarkers;
        String markersRaw = launchConfig.getHeadlessMarker() != null
                ? launchConfig.getHeadlessMarker()
                : legacyMarker ? LEGACY_HEADLESS_MARKER : DEFAULT_HEADLESS_MARKERS;
        long gracePeriod = legacyMarker ? LEGACY_HEADLESS_GRACE_PERIOD_NANOS : HEADLESS_GRACE_PERIOD_NANOS;

        String[] markers = markersRaw.split(",");

        long timeoutSeconds = launchConfig.getHeadlessTimeoutSeconds();
        long deadline = System.nanoTime() + timeoutSeconds * 1_000_000_000L;

        AtomicLong matchedAt = new AtomicLong(-1);
        AtomicBoolean crashed = new AtomicBoolean(false);

        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                // lines left until we know if a crash report header belongs to a real crash or ist just forge pretending again
                int crashReportLinesLeft = -1;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (crashReportLinesLeft >= 0) {
                        if (line.contains(FAKE_CRASH_REPORT_DESCRIPTION)) {
                            crashReportLinesLeft = -1;
                        } else if (crashReportLinesLeft-- == 0) {
                            crashed.set(true);
                        }
                    }
                    for (String crashMarker : HEADLESS_CRASH_MARKERS) {
                        if (!line.contains(crashMarker)) {
                            continue;
                        }
                        if (crashMarker.equals(CRASH_REPORT_HEADER)) {
                            crashReportLinesLeft = CRASH_REPORT_DESCRIPTION_LINES;
                        } else {
                            crashed.set(true);
                        }
                    }
                    if (matchedAt.get() < 0 && !crashed.get()) {
                        for (String marker : markers) {
                            if (line.contains(marker.trim())) {
                                matchedAt.set(System.nanoTime());
                                break;
                            }
                        }
                    }
                }
                if (crashReportLinesLeft >= 0) {
                    crashed.set(true);
                }
            } catch (IOException ignored) {
            }
        }, "minified-headless-log-reader");
        readerThread.setDaemon(true);
        readerThread.start();

        boolean stoppedByUs = false;
        while (process.isAlive()) {
            if (crashed.get()) {
                System.err.println("[minified-headless] game crashed, stopping process");
                terminateProcess(process);
                break;
            }
            long matched = matchedAt.get();
            // keep watching for a crash for a moment after the marker, then stop the game
            if (matched >= 0 && System.nanoTime() - matched > gracePeriod) {
                stoppedByUs = true;
                terminateProcess(process);
                break;
            }
            if (matched < 0 && System.nanoTime() > deadline) {
                System.err.println("[minified-headless] timeout after "
                        + timeoutSeconds + "s, killing process");
                process.destroyForcibly();
                process.waitFor();
                return 124;
            }
            process.waitFor(500, TimeUnit.MILLISECONDS);
        }

        process.waitFor();
        readerThread.join(TimeUnit.SECONDS.toMillis(5));

        int exitCode = process.exitValue();
        if (crashed.get()) {
            return exitCode != 0 ? exitCode : 1;
        }
        if (matchedAt.get() >= 0) {
            if (stoppedByUs) {
                return 0;
            }
            // nothing closes the game in headless mode, so it died during the grace period
            System.err.println("[minified-headless] game exited right after reaching a marker");
            return exitCode != 0 ? exitCode : 1;
        }
        System.err.println("[minified-headless] game exited before reaching a marker");
        return exitCode != 0 ? exitCode : 1;
    }

    private static void terminateProcess(Process process) {
        process.destroy();
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                System.err.println("[minified-headless] graceful stop timed out, forcing kill");
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }

    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public interface ProfileSupplier {
        JsonObject get();
    }
}