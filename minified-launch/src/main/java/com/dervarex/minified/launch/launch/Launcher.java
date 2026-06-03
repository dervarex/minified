package com.dervarex.minified.launch.launch;

import com.dervarex.minified.auth.User;
import com.dervarex.minified.java.JavaInstallation;
import com.dervarex.minified.java.JavaManager;
import com.dervarex.minified.launch.arguments.GameArgumentsParser;
import com.dervarex.minified.launch.arguments.JvmArgumentsParser;
import com.dervarex.minified.launch.download.ClientDownloader;
import com.dervarex.minified.launch.download.assets.AssetDownloader;
import com.dervarex.minified.launch.download.libraries.LibraryDownloader;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
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

            JsonFile versionJson =
                    new JsonFile(
                            HttpUtil.get(
                                    VersionMetadataProvider
                                            .getVersionJsonUrl(version)
                            )
                    );

            JavaInstallation javaInstallation =
                    JavaManager.ensureJavaVersion(
                            JavaManager.getRequiredJavaVersion(versionJson)
                    );

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

            JsonObject arguments =
                    versionJson
                            .get("arguments")
                            .asObject();

            List<String> jvmArgs =
                    buildJvmArguments(
                            arguments,
                            launchConfig,
                            options,
                            loader,
                            version
                    );

            List<String> gameArgs =
                    buildGameArguments(
                            arguments,
                            options,
                            loader,
                            version
                    );

            ArrayList<String> command =
                    new ArrayList<>();

            command.add(
                      javaInstallation
                              .executable()
                              .toAbsolutePath()
                              .toString()

            );                                                     // java
            command.addAll(jvmArgs);                               // jvmargs
            command.add(getMainClass(versionJson,loader,version)); // mainclass
            command.addAll(gameArgs);                              // gameargs

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
            JsonObject arguments,
            LaunchConfigurator launchConfig,
            LaunchOptions options,
            Loader loader,
            String version
    ) {

        JsonArray mergedJvm =
                new JsonArray();

        JsonValue defaultUserJvmValue =
                arguments.get("default-user-jvm");

        if (defaultUserJvmValue != null) {
            for (JsonValue value : defaultUserJvmValue.asArray()) {
                mergedJvm.add(value);
            }
        }

        JsonValue jvmValue =
                arguments.get("jvm");

        if (jvmValue != null) {
            for (JsonValue value : jvmValue.asArray()) {
                mergedJvm.add(value);
            }
        }

        List<String> jvmArgs =
                JvmArgumentsParser.parse(
                        mergedJvm,
                        launchConfig.getMinRam(),
                        launchConfig.getMaxRam()
                );

        jvmArgs.addAll(
                launchConfig.getExtraJvmArgs()
        );
        switch (loader) {
            case Vanilla:
                // Nothing additional required here
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
//            case Quilt: // quilt currently doesn't seem to require additional jvm arguments, but we'll keep this here in case they add some in the future
//                try {
//                    JsonObject quiltProfileJson = QuiltLoaderFetcher.getLatestProfile(version);
//                    JsonValue quiltArguments = quiltProfileJson.asObject().get("arguments");
//
//                    for (JsonValue e : quiltArguments.asObject().get("jvm").asArray()) {
//                        jvmArgs.add(e.asString());
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException("Failed to fetch Quilt loader profile", e);
//                }
        }

        return X11Helper.substituteVariables(
                jvmArgs,
                options.getVariables()
        );
    }

    private static List<String> buildGameArguments(
            JsonObject arguments,
            LaunchOptions options,
            Loader loader,
            String version
    ) {

        JsonValue gameValue = arguments.get("game");

        JsonArray gameArray =
                gameValue != null
                        ? gameValue.asArray()
                        : new JsonArray();

        switch (loader) {
            case Vanilla:
                // nothing additional required here
                break;
            case Fabric:
                try {
                    JsonObject fabricProfileJson = FabricLoaderFetcher.getLatestProfile(version);
                    JsonValue fabricArguments = fabricProfileJson.get("arguments");

                    if (fabricArguments != null) {
                        JsonArray fabricGameArgs =
                                fabricArguments.asObject()
                                        .get("game")
                                        .asArray();

                        for (JsonValue arg : fabricGameArgs) {
                            gameArray.add(arg);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch Fabric loader profile", e);
                }
                break;
            case Quilt: // quilt currently doesn't have any game args(empty array), but in case they add arguments, this should work
                try {
                    JsonObject quiltProfileJson = QuiltLoaderFetcher.getLatestProfile(version);
                    JsonValue quiltArguments = quiltProfileJson.get("arguments");

                    if (quiltArguments != null) {
                        JsonArray quiltGameArgs =
                                quiltArguments.asObject()
                                        .get("game")
                                        .asArray();

                        for (JsonValue arg : quiltGameArgs) {
                            gameArray.add(arg);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to fetch Quilt loader profile", e);
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
            String version
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

    public static void main(String[] args) throws HttpException, IOException {
        JsonFile versionJson =
                new JsonFile(
                        HttpUtil.get(
                                VersionMetadataProvider
                                        .getVersionJsonUrl("1.21.11")
                        )
                );
        System.out.println(versionJson.getRoot());
    }
}
