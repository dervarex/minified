package com.dervarex.minified.launch.launch;

import com.dervarex.minified.auth.User;
import com.dervarex.minified.java.JavaInstallation;
import com.dervarex.minified.java.JavaManager;
import com.dervarex.minified.launch.arguments.GameArgumentsParser;
import com.dervarex.minified.launch.arguments.JvmArgumentsParser;
import com.dervarex.minified.launch.arguments.LegacyMinecraftArgumentsParser;
import com.dervarex.minified.launch.download.ClientDownloader;
import com.dervarex.minified.launch.download.assets.AssetDownloader;
import com.dervarex.minified.launch.download.libraries.LibraryDownloader;
import com.dervarex.minified.events.type.connection.CheckConnectionEvent;
import com.dervarex.minified.launch.events.launch.GameStoppedEvent;
import com.dervarex.minified.launch.events.launch.GameStartEvent;
import com.dervarex.minified.events.type.connection.OfflineEvent;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.custom.CustomLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionFetcher;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionJson;
import com.dervarex.minified.launch.launch.modding.forge.installer.ForgeInstallerInjector;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionFetcher;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionJson;
import com.dervarex.minified.launch.launch.modding.neoforge.installer.NeoInstallerInjector;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.launch.utils.X11Helper;
import com.dervarex.minified.launch.version.VersionMetadataProvider;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.*;
import com.dervarex.minified.utils.network.NetworkUtil;

import java.io.File;
import java.io.IOException;
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
    public static void launchMinecraft(
            User user,
            LaunchConfiguration launchConfig) {
        LaunchContext context = new LaunchContext(user, launchConfig);

        try {
            Loader loader = launchConfig.getLoader();

            context.setOnline(true);
            try {
                context.getEventBus().post(new CheckConnectionEvent());
                NetworkUtil.ensureOnline("launch minecraft");
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

            JsonFile versionJson = loadVersionJson(loader.mcVersion(), context.isOnline());

            JavaInstallation javaInstallation;
            if (launchConfig.getCustomJavaExecutable() == null) {
                //context.getEventBus().post(new EnsureJavaEvent());
                javaInstallation =
                        JavaManager.ensureJavaVersion(
                                JavaManager.getRequiredJavaVersion(versionJson)
                        );
            } else {
                javaInstallation = null;
            }

            downloadFiles(
                    loader.mcVersion(),
                    context
            );

            String classpath =
                    ClasspathBuilder.buildClasspath(
                            versionJson,
                            launchConfig
                    );
            if (loader instanceof CustomLoader customLoader && customLoader.customClasspathEntries() != null) {
                StringBuilder cpBuilder = new StringBuilder(classpath);
                for (String entry : customLoader.customClasspathEntries()) {
                    if (cpBuilder.length() > 0) {
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
                    buildJvmArguments(
                            versionJson,
                            launchConfig,
                            options,
                            loader,
                            loader.mcVersion(),
                            context.isOnline()
                    ); // includes the classpath
            Path nativesDir =
                    launchConfig.getLibrariesDirectory()
                            .toAbsolutePath()
                            .getParent()
                            .resolve("natives");

            jvmArgs.add(
                    "-Djava.library.path=" + nativesDir
            );

            jvmArgs.add(
                    "-Dorg.lwjgl.librarypath=" + nativesDir
            );

            List<String> gameArgs =
                    buildGameArguments(
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
            command.addAll(jvmArgs);                                                                        // -Dsomearg -cp ...
            command.add   (getMainClass(versionJson, loader, loader.mcVersion(), launchConfig, context.isOnline()));// net.minecraft.client.main.Main
            command.addAll(gameArgs);                                                                       // --username ... --accessToken ...

            launchProcess(command, context);

        } catch (HttpException | IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static void downloadFiles( // todo move to a different file
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
                launchConfig.getLibrariesDirectory()
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
                    "Missing cached client jar: " + launchConfig.getJarFile()
            );
        }
    }

    private static List<String> buildJvmArguments(
            JsonFile versionJson,
            LaunchConfiguration launchConfig,
            LaunchOptions options,
            Loader loader,
            String version,
            boolean online
    ) {
        JsonArray mergedJvm = new JsonArray();

        JsonValue argumentsValue = versionJson.get("arguments");
        if (argumentsValue != null) {
            JsonObject arguments = argumentsValue.asObject();

            JsonValue defaultUserJvmValue = arguments.get("default-user-jvm");
            if (defaultUserJvmValue != null) {
                for (JsonValue value : defaultUserJvmValue.asArray()) {
                    mergedJvm.add(value);
                }
            }

            JsonValue jvmValue = arguments.get("jvm");
            if (jvmValue != null) {
                for (JsonValue value : jvmValue.asArray()) {
                    mergedJvm.add(value);
                }
            }
        }

        List<String> jvmArgs = JvmArgumentsParser.parse(
                mergedJvm,
                launchConfig.getMinRam(),
                launchConfig.getMaxRam()
        );

        jvmArgs.addAll(launchConfig.getExtraJvmArgs());
        jvmArgs.removeIf(arg -> arg.equals("-XX:+UseCompactObjectHeaders")); // I don't know if we should do it like that, but it seems to work fine
        jvmArgs.removeIf(arg ->
                arg.equals("--sun-misc-unsafe-memory-access=allow"));

        if (loader instanceof CustomLoader customLoader) {
            if (customLoader.customJvmArgs() != null) {
                jvmArgs.addAll(customLoader.customJvmArgs());
            }
        } else {
            JsonObject loaderProfileJson = null;

            switch (loader) {
                case VanillaLoader ignored:
                    break;
                case FabricLoader ignored:
                    loaderProfileJson = loadFabricProfileJson(version, online);
                    break;
                case ForgeLoader ignored:
                    loaderProfileJson = loadForgeProfileJson(version, launchConfig, online);
                    break;
                case NeoforgeLoader ignored:
                    loaderProfileJson = loadNeoForgeProfileJson(version, launchConfig, online);
                    break;
                case QuiltLoader ignored:
                    loaderProfileJson = loadQuiltProfileJson(version, online);
                    break;
                default:
                    throw new IllegalStateException("Unexpected loader: " + loader);
            }

            if (loaderProfileJson != null) {
                JsonValue fabricArguments = loaderProfileJson.get("arguments");
                if (fabricArguments != null && fabricArguments.asObject().get("jvm") != null) {
                    for (JsonValue e : fabricArguments.asObject().get("jvm").asArray()) {
                        jvmArgs.add(e.asString());
                    }
                }
            }
        }

        return X11Helper.substituteVariables(jvmArgs, options.getVariables());
    }

    private static List<String> buildGameArguments(
            JsonFile versionJson,
            LaunchOptions options,
            Loader loader,
            String version,
            LaunchConfiguration launchConfig,
            boolean online
    ) {
        JsonValue argumentsValue = versionJson.get("arguments");

        if (argumentsValue == null) {
            JsonValue minecraftArguments = versionJson.get("minecraftArguments");
            if (minecraftArguments == null) {
                throw new RuntimeException("No arguments or minecraftArguments found in version JSON");
            }

            return X11Helper.substituteVariables(
                    LegacyMinecraftArgumentsParser.parse(minecraftArguments.asString()),
                    options.getVariables()
            );
        }

        JsonObject arguments = argumentsValue.asObject();

        JsonValue gameValue = arguments.get("game");
        JsonArray gameArray = gameValue != null ? gameValue.asArray() : new JsonArray();

        if (loader instanceof CustomLoader customLoader) {
            if (customLoader.customGameArgs() != null) {
                for (String arg : customLoader.customGameArgs()) {
                    gameArray.add(JsonParser.parse(arg));
                }
            }
        } else {
            JsonObject loaderProfileJson = null;

            switch (loader) {
                case VanillaLoader ignored:
                    break;
                case FabricLoader ignored:
                    loaderProfileJson = loadFabricProfileJson(version, online);
                    break;
                case QuiltLoader ignored:
                    loaderProfileJson = loadQuiltProfileJson(version, online);
                    break;
                case ForgeLoader ignored:
                    loaderProfileJson = loadForgeProfileJson(version, launchConfig, online);
                    break;
                case NeoforgeLoader ignored:
                    loaderProfileJson = loadNeoForgeProfileJson(version, launchConfig, online);
                    break;
                default:
                    throw new IllegalStateException("Unexpected loader: " + loader);
            }

            if (loaderProfileJson != null) {
                JsonValue loaderArguments = loaderProfileJson.get("arguments");
                if (loaderArguments != null) {
                    JsonValue loaderGame = loaderArguments.asObject().get("game");
                    if (loaderGame != null) {
                        for (JsonValue arg : loaderGame.asArray()) {
                            gameArray.add(arg);
                        }
                    }
                }
            }
        }

        return GameArgumentsParser.parse(
                gameArray,
                options.getVariables(),
                options.getFeatures()
        );
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
            case FabricLoader ignored -> loadFabricProfileJson(version, online).get("mainClass");
            case QuiltLoader ignored -> loadQuiltProfileJson(version, online).get("mainClass");
            case ForgeLoader ignored -> loadForgeProfileJson(version, launchConfig, online).get("mainClass");
            case NeoforgeLoader ignored -> loadNeoForgeProfileJson(version, launchConfig, online).get("mainClass");
            default -> throw new IllegalStateException("Unexpected loader: " + loader);
        };

        if (mainClassValue == null) {
            throw new RuntimeException(
                    "Main class not found in version JSON"
            );
        }

        return mainClassValue.asString();
    }

    private static JsonFile loadVersionJson(String version, boolean online) throws IOException {
        Path cachePath = cachedVersionJsonPath(version);

        if (Files.exists(cachePath)) {
            try {
                return new JsonFile(Files.readString(cachePath));
            } catch (Exception ignored) {
                // broken cache, fall through to network if possible
            }
        }

        if (!online) {
            throw new OfflineModeNeedsNetworkException(
                    "Missing cached version JSON: " + cachePath
            );
        }

        String versionJsonUrl;
        String raw;
        try {
            versionJsonUrl = VersionMetadataProvider.getVersionJsonUrl(version);

            raw = HttpUtil.get(versionJsonUrl);
        } catch (HttpException e) {
            throw new RuntimeException(e);
        }
        writeCache(cachePath, raw);
        return new JsonFile(raw);
    }

    private static JsonObject loadFabricProfileJson(String version, boolean online) {
        return loadProfileJson(
                version,
                "fabric",
                online,
                () -> {
                    try {
                        return FabricLoaderFetcher.getLatestProfile(version);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private static JsonObject loadQuiltProfileJson(String version, boolean online) {
        return loadProfileJson(
                version,
                "quilt",
                online,
                () -> {
                    try {
                        return QuiltLoaderFetcher.getLatestProfile(version);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private static JsonObject loadForgeProfileJson(
            String version,
            LaunchConfiguration launchConfig,
            boolean online
    ) {
        return loadProfileJson(
                version,
                "forge",
                online,
                () -> {
                    try {
                        Path parent = launchConfig.getJarFile().getParent().toAbsolutePath();
                        String latest = new ForgeVersionFetcher().getLatest(version);
                        JsonFile forgeVersionJson = ForgeVersionJson.getVersionJson(parent, latest);
                        return forgeVersionJson.asObject();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private static JsonObject loadNeoForgeProfileJson(
            String version,
            LaunchConfiguration launchConfig,
            boolean online
    ) {
        return loadProfileJson(
                version,
                "neoforge",
                online,
                () -> {
                    try {
                        Path parent = launchConfig.getJarFile().getParent().toAbsolutePath();
                        String latest = new NeoVersionFetcher().getLatest(version);
                        JsonFile neoVersionJson = NeoVersionJson.getVersionJson(parent, latest);
                        return neoVersionJson.asObject();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private static JsonObject loadProfileJson(
            String version,
            String loaderName,
            boolean online,
            ProfileSupplier supplier
    ) {
        Path cachePath = cachedProfileJsonPath(loaderName, version);

        if (Files.exists(cachePath)) {
            try {
                return new JsonFile(Files.readString(cachePath)).asObject();
            } catch (Exception ignored) {
                // broken cache, fall through
            }
        }

        if (!online) {
            throw new OfflineModeNeedsNetworkException(
                    "Missing cached " + loaderName + " profile: " + cachePath
            );
        }

        JsonObject profile = supplier.get();
        try {
            writeCache(cachePath, profile.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to cache " + loaderName + " profile", e);
        }
        return profile;
    }

    private static Path cachedProfileJsonPath(String loaderName, String version) {
        return cacheRoot()
                .resolve("profiles")
                .resolve(loaderName)
                .resolve(version + ".json");
    }

    private static Path cachedVersionJsonPath(String version) {
        return cacheRoot()
                .resolve("versions")
                .resolve(version + ".json");
    }

    private static Path cacheRoot() {
        return JavaManager.getBaseDir().resolve("cache");
    }

    private static void writeCache(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private interface ProfileSupplier {
        JsonObject get();
    }

    private static void launchProcess(
            List<String> command,
            LaunchContext context
    ) throws IOException, InterruptedException {

        ProcessBuilder processBuilder =
                new ProcessBuilder(command);
        X11Helper.configureGraphicsEnvironment(
                processBuilder,
                context
        );

        System.out.println(
                String.join(" ", command)
        );

        processBuilder.inheritIO();

        context.getEventBus().post(new GameStartEvent(
                context.getUser(),
                context.getLaunchConfiguration(),
                context.isOnline()
        ));

        Process process = processBuilder.start();

        int exitCode = process.waitFor();
        context.getEventBus().post(new GameStoppedEvent(exitCode, context.getLaunchConfiguration()));
//        if (exitCode != 0) {
//            throw new RuntimeException(
//                    "Minecraft exited with code " + exitCode
//            );
//        }
    }
}