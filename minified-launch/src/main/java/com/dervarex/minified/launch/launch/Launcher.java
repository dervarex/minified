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
import com.dervarex.minified.launch.events.launch.GameOutputEvent;
import com.dervarex.minified.launch.events.launch.GameProcessStartedEvent;
import com.dervarex.minified.launch.events.launch.GameStartEvent;
import com.dervarex.minified.launch.events.launch.GameStoppedEvent;
import com.dervarex.minified.launch.exceptions.loader.UnexpectedLoaderException;
import com.dervarex.minified.launch.exceptions.version.MalformedVersionJsonException;
import com.dervarex.minified.launch.launch.internal.ArgumentsBuilder;
import com.dervarex.minified.launch.launch.internal.CacheManager;
import com.dervarex.minified.launch.launch.internal.HeadlessWatcher;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
                Path gameDir = launchConfig.resolveGameDirectory();
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
                context
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
        Path gameDir = launchConfig.resolveGameDirectory();
        Files.createDirectories(gameDir);
        processBuilder.directory(gameDir.toFile());
        X11Helper.configureGraphicsEnvironment(
                processBuilder,
                context
        );

        boolean capture = launchConfig.isCaptureGameOutput() && !launchConfig.isHeadless();
        if (launchConfig.isHeadless()) {
            processBuilder.redirectErrorStream(true);
        } else if (!capture) {
            processBuilder.inheritIO();
        }

        context.getEventBus().post(new GameStartEvent(
                context.getUser(),
                context.getLaunchConfiguration(),
                context.isOnline()
        ));

        Process process = processBuilder.start();
        context.getEventBus().post(new GameProcessStartedEvent(process, launchConfig));

        List<Thread> outputReaders = capture
                ? List.of(
                        forwardOutput(process.getInputStream(), GameOutputEvent.Stream.STDOUT, context),
                        forwardOutput(process.getErrorStream(), GameOutputEvent.Stream.STDERR, context))
                : List.of();

        int exitCode = launchConfig.isHeadless()
                ? HeadlessWatcher.forLaunch(launchConfig, logsNoMarkers).watch(process)
                : process.waitFor();

        // the last lines (the crash report, usually) should arrive before anyone hears that the game stopped
        for (Thread reader : outputReaders) {
            reader.join();
        }

        context.getEventBus().post(new GameStoppedEvent(exitCode, context.getLaunchConfiguration()));
//        if (exitCode != 0) {
//            throw new RuntimeException(
//                    "Minecraft exited with code " + exitCode
//            );
//        } todo: NonZeroExitCodeException or NonZeroExitCodeEvent?
    }

    private static Thread forwardOutput(InputStream stream, GameOutputEvent.Stream type, LaunchContext context) {
        Thread reader = new Thread(() -> {
            try (BufferedReader lines = new BufferedReader(new InputStreamReader(stream, Charset.defaultCharset()))) {
                String line;
                while ((line = lines.readLine()) != null) {
                    context.getEventBus().post(new GameOutputEvent(line, type));
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Lost the game's " + type.name().toLowerCase() + " stream", e);
            }
        }, "GameOutput-" + type.name().toLowerCase());
        reader.setDaemon(true);
        reader.start();
        return reader;
    }

    @API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
    public interface ProfileSupplier {
        JsonObject get();
    }
}