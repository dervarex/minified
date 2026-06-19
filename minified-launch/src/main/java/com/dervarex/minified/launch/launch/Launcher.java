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
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionFetcher;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionJson;
import com.dervarex.minified.launch.launch.modding.forge.installer.ForgeInstallerInjector;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionFetcher;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionJson;
import com.dervarex.minified.launch.launch.modding.neoforge.installer.NeoInstallerInjector;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher;
import com.dervarex.minified.launch.utils.X11Helper;
import com.dervarex.minified.launch.version.VersionMetadataProvider;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.exceptions.NoConnectionException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.network.NetworkUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Launcher {
    /**
     * Downloads required files and launches Minecraft.
     *
     * @param version the Minecraft version to launch
     * @param user    the logged-in user to launch with, or null to launch in offline mode(you won't be able to join online servers or use any online features in offline mode)
     * @param launchConfig configuration used by the launcher when starting the game,
     *                     such as the number of download threads, launcher name,
     *                     and launcher version
     *<p>
     *                     Example:
     *                     <pre>{@code
     *                  LaunchConfigurator config = new LaunchConfigurator.Builder()
     *                 .downloadThreads(10)
     *                 .launcherName("MinifiedLauncher")
     *                 .launcherVersion("1.0.0")
     *                 .assetsDirectory(Path.of("path to assets directory"))
     *                 .librariesDirectory(Path.of("path to library directory"))
     *                 .jarFile(Path.of("path to client.jar"))
     *                 .build();
     * }</pre>
     */
    public static void launchMinecraft(
            String version,
            User user,
            LaunchConfigurator launchConfig) {

        try {

            NetworkUtil.ensureOnline("launch minecraft");

            Loader loader = launchConfig.getLoader();
            if(loader.equals(Loader.Forge)) {
                ForgeInstallerInjector forgeInstallerInjector = new ForgeInstallerInjector();
                forgeInstallerInjector.install(launchConfig, version);
            } else if (loader.equals(Loader.NeoForge)) {
                NeoInstallerInjector neoInstallerInjector = new NeoInstallerInjector();
                neoInstallerInjector.install(launchConfig, version);

//                String neoForgeVersion = new NeoVersionFetcher().getLatest(version);
//                ensureNeoForgeCommonJar(launchConfig, version, neoForgeVersion);
            }

            JsonFile versionJson =
                    new JsonFile(
                            HttpUtil.get(
                                    VersionMetadataProvider
                                            .getVersionJsonUrl(version)
                            )
                    );
            JavaInstallation javaInstallation;
            if(launchConfig.getCustomJavaExecutable() == null) {
                javaInstallation =
                        JavaManager.ensureJavaVersion(
                                JavaManager.getRequiredJavaVersion(versionJson)
                        );
            } else {
                javaInstallation = null;
            }

            downloadFiles(
                    version,
                    launchConfig
            );

            String classpath =
                    ClasspathBuilder.buildClasspath(
                            versionJson,
                            launchConfig
                    );

            LaunchOptions options =
                    LaunchOptions.buildLaunchOptions(
                            user,
                            version,
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
                            version
                    );
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
                            version,
                            launchConfig
                    );

            ArrayList<String> command =
                    new ArrayList<>();

            command.add(
                    javaInstallation != null
                            ? javaInstallation.executable().toAbsolutePath().toString()
                            : launchConfig.getCustomJavaExecutable().toAbsolutePath().toString()
            );

            command.addAll(jvmArgs);

//            command.add("-cp");
//            command.add(classpath);

            command.add(
                    getMainClass(
                            versionJson,
                            loader,
                            version,
                            launchConfig
                    )
            );

            command.addAll(gameArgs);                                   // gameargs

            launchProcess(command);

        } catch (HttpException | IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (NoConnectionException e) {
            System.err.println("You do not have a internet connection");
        }
    }

    private static void downloadFiles(
            String version,
            LaunchConfigurator launchConfig
    ) throws HttpException, IOException {

        // Downloads (will skip files if they already exist)
        LibraryDownloader libDownloader    = new LibraryDownloader(launchConfig.getDownloadThreads());
        AssetDownloader   assetDownloader  = new AssetDownloader(launchConfig.getDownloadThreads());
        ClientDownloader  clientDownloader = new ClientDownloader();

        libDownloader.downloadLibraries(
                version,
                launchConfig.getLoader(),
                launchConfig.getLibrariesDirectory()
        );

        assetDownloader.downloadAssets(
                version,
                launchConfig.getAssetsDirectory()
        );

        clientDownloader.downloadClient(
                version,
                launchConfig.getJarFile()
        );
    }

    private static List<String> buildJvmArguments(
            JsonFile versionJson,
            LaunchConfigurator launchConfig,
            LaunchOptions options,
            Loader loader,
            String version
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
        jvmArgs.removeIf(arg -> arg.equals("-XX:+UseCompactObjectHeaders")); //to do is this correct? - seems like it works or smth
        jvmArgs.removeIf(arg ->
                arg.equals("--sun-misc-unsafe-memory-access=allow"));

        switch (loader) {
            case Vanilla:
                break;
            case Fabric:
                try {
                    JsonObject fabricProfileJson = FabricLoaderFetcher.getLatestProfile(version);
                    JsonValue fabricArguments = fabricProfileJson.asObject().get("arguments");
                    for (JsonValue e : fabricArguments.asObject().get("jvm").asArray()) {
                        jvmArgs.add(e.asString());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch Fabric loader profile", e);
                }
                break;
            case Forge:
                try {
                    JsonFile forgeVersionJson = ForgeVersionJson.getVersionJson(
                            launchConfig.getJarFile().getParent().toAbsolutePath(),
                            new ForgeVersionFetcher().getLatest(version)
                    );
                    JsonValue forgeArguments = forgeVersionJson.asObject().get("arguments");
                    for (JsonValue e : forgeArguments.asObject().get("jvm").asArray()) {
                        jvmArgs.add(e.asString());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch Forge loader profile", e);
                }
                break;
            case NeoForge:
                try {
                    JsonFile neoVersionJson =
                            NeoVersionJson.getVersionJson(
                                    launchConfig.getJarFile().getParent().toAbsolutePath(),
                                    new NeoVersionFetcher().getLatest(version)
                            );

                    JsonValue neoArguments =
                            neoVersionJson.asObject().get("arguments");

                    for (JsonValue e : neoArguments.asObject().get("jvm").asArray()) {
                        jvmArgs.add(e.asString());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch NeoForge loader profile", e);
                }
                break;
        }

        return X11Helper.substituteVariables(jvmArgs, options.getVariables());
    }

    private static List<String> buildGameArguments(
            JsonFile versionJson,
            LaunchOptions options,
            Loader loader,
            String version,
            LaunchConfigurator launchConfig
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

        switch (loader) {
            case Vanilla:
                break;
            case Fabric:
                try {
                    JsonObject fabricProfileJson = FabricLoaderFetcher.getLatestProfile(version);
                    JsonValue fabricArguments = fabricProfileJson.get("arguments");

                    if (fabricArguments != null) {
                        JsonArray fabricGameArgs = fabricArguments.asObject().get("game").asArray();
                        for (JsonValue arg : fabricGameArgs) {
                            gameArray.add(arg);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch Fabric loader profile", e);
                }
                break;
            case Quilt:
                try {
                    JsonObject quiltProfileJson = QuiltLoaderFetcher.getLatestProfile(version);
                    JsonValue quiltArguments = quiltProfileJson.get("arguments");

                    if (quiltArguments != null) {
                        JsonArray quiltGameArgs = quiltArguments.asObject().get("game").asArray();
                        for (JsonValue arg : quiltGameArgs) {
                            gameArray.add(arg);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch Quilt loader profile", e);
                }
                break;
            case Forge:
                try {
                    JsonFile forgeVersionJson = ForgeVersionJson.getVersionJson(
                            launchConfig.getJarFile().getParent().toAbsolutePath(),
                            new ForgeVersionFetcher().getLatest(version)
                    );
                    JsonValue forgeArguments = forgeVersionJson.asObject().get("arguments");
                    for (JsonValue a : forgeArguments.asObject().get("game").asArray()) {
                        gameArray.add(a);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch Forge loader profile", e);
                }
                break;
            case NeoForge:
                try {
                    JsonFile neoVersionJson =
                            NeoVersionJson.getVersionJson(
                                    launchConfig.getJarFile().getParent().toAbsolutePath(),
                                    new NeoVersionFetcher().getLatest(version)
                            );

                    JsonValue neoArguments =
                            neoVersionJson.asObject().get("arguments");

                    for (JsonValue a : neoArguments.asObject().get("game").asArray()) {
                        gameArray.add(a);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch NeoForge loader profile", e);
                }
                break;
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
            LaunchConfigurator launchConfig
    ) {
        JsonValue mainClassValue;
        switch (loader) {
            case Vanilla:
                mainClassValue =
                        versionJson.get("mainClass");
                break;
            case Fabric:
                try {
                    JsonObject fabricProfileJson = FabricLoaderFetcher.getLatestProfile(version);
                    mainClassValue = fabricProfileJson.get("mainClass");
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch Fabric loader profile", e);
                }
                break;
            case Quilt:
                try {
                    JsonObject quiltProfileJson = QuiltLoaderFetcher.getLatestProfile(version);
                    mainClassValue = quiltProfileJson.get("mainClass");
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch Quilt loader profile", e);
                }
                break;
            case Forge:
                try {
                    JsonObject forgeVersionJson = ForgeVersionJson.getVersionJson(launchConfig.getJarFile().getParent().toAbsolutePath(), new ForgeVersionFetcher().getLatest(version)).asObject();
                    mainClassValue = forgeVersionJson.get("mainClass");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case NeoForge:
                try {
                    JsonObject neoVersionJson =
                            NeoVersionJson.getVersionJson(
                                    launchConfig.getJarFile().getParent().toAbsolutePath(),
                                    new NeoVersionFetcher().getLatest(version)
                            ).asObject();

                    mainClassValue = neoVersionJson.get("mainClass");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                throw new RuntimeException("Unknown loader " + loader);
        }



        if (mainClassValue == null) {
            throw new RuntimeException(
                    "Main class not found in version JSON"
            );
        }

        return mainClassValue.asString();
    }

    private static void launchProcess(
            List<String> command
    ) throws IOException, InterruptedException {

        ProcessBuilder processBuilder =
                new ProcessBuilder(command);

        X11Helper.configureGraphicsEnvironment(
                processBuilder
        );

        System.out.println(
                String.join(" ", command)
        );

        processBuilder.inheritIO();

        Process process =
                processBuilder.start();

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException(
                    "Minecraft exited with code " + exitCode
            );
        }
    }
//    static void ensureNeoForgeCommonJar(
//            LaunchConfigurator config,
//            String minecraftVersion,
//            String neoForgeVersion
//    ) throws IOException {
//        Path base = config.getLibrariesDirectory();
//
//        Path patchedJar = base.resolve("net/neoforged/minecraft-client-patched")
//                .resolve(neoForgeVersion)
//                .resolve("minecraft-client-patched-" + neoForgeVersion + ".jar");
//
//        Path targetJar = base.resolve("net/minecraft/client")
//                .resolve(minecraftVersion + "-1")
//                .resolve("client-" + minecraftVersion + "-1-srg.jar");
//
//        if (Files.exists(targetJar)) {
//            return;
//        }
//
//        Files.createDirectories(targetJar.getParent());
//
//        try (java.util.jar.JarFile source = new java.util.jar.JarFile(patchedJar.toFile())) {
//            java.util.jar.Manifest manifest = source.getManifest();
//            if (manifest == null) {
//                manifest = new java.util.jar.Manifest();
//            }
//
//            java.util.jar.Attributes attrs = manifest.getMainAttributes();
//            if (attrs.getValue(java.util.jar.Attributes.Name.MANIFEST_VERSION) == null) {
//                attrs.put(java.util.jar.Attributes.Name.MANIFEST_VERSION, "1.0");
//            }
//
//            attrs.putValue("Minecraft-Dists", "CLIENT"); // we have to put it in there for neoforge to work  to do: did it?  -  um no it did not  -  maybe if we skip some shit?
//
//            try (java.util.jar.JarOutputStream out =
//                         new java.util.jar.JarOutputStream(Files.newOutputStream(targetJar), manifest)) {
//                var entries = source.entries();
//                while (entries.hasMoreElements()) {
//                    var entry = entries.nextElement();
//                    if ("META-INF/MANIFEST.MF".equals(entry.getName())) {
//                        continue;
//                    }
//
//                    out.putNextEntry(new java.util.jar.JarEntry(entry.getName()));
//                    try (var in = source.getInputStream(entry)) {
//                        in.transferTo(out);
//                    }
//                    out.closeEntry();
//                }
//            }
//        }
//    }
}
