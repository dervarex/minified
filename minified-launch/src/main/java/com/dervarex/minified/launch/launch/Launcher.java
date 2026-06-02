package com.dervarex.minified.launch.launch;

import com.dervarex.minified.auth.User;
import com.dervarex.minified.java.JavaInstallation;
import com.dervarex.minified.java.JavaManager;
import com.dervarex.minified.launch.arguments.GameArgumentsParser;
import com.dervarex.minified.launch.arguments.JvmArgumentsParser;
import com.dervarex.minified.launch.download.ClientDownloader;
import com.dervarex.minified.launch.download.assets.AssetDownloader;
import com.dervarex.minified.launch.download.libraries.LibraryDownloader;
//import com.dervarex.minified.launch.patch.JarPatcher;
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

            // Downloads (will skip files if they already exist)
            LibraryDownloader libDownloader = new LibraryDownloader(launchConfig.getDownloadThreads());
            AssetDownloader assetDownloader = new AssetDownloader(launchConfig.getDownloadThreads());
            ClientDownloader clientDownloader = new ClientDownloader();

            libDownloader.downloadLibraries(version, launchConfig.getLibrariesDirectory());
            assetDownloader.downloadAssets(version, launchConfig.getAssetsDirectory());
            clientDownloader.downloadClient(version, launchConfig.getJarFile());

            String separator =
                    System.getProperty("os.name")
                            .toLowerCase()
                            .contains("win")
                            ? ";"
                            : ":";

            ArrayList<String> classpath =
                    new ArrayList<>();

            classpath.add(
                    launchConfig.getJarFile()
                            .toAbsolutePath()
                            .toString()
            );

            JsonArray libraries =
                    versionJson
                            .get("libraries")
                            .asArray();

            for (JsonValue value : libraries) {

                JsonObject library =
                        value.asObject();

                JsonValue downloadsValue =
                        library.get("downloads");

                if (downloadsValue == null) {
                    continue;
                }

                JsonObject downloads =
                        downloadsValue.asObject();

                JsonValue artifactValue =
                        downloads.get("artifact");

                if (artifactValue == null) {
                    continue;
                }

                JsonObject artifact =
                        artifactValue.asObject();

                String path =
                        artifact
                                .get("path")
                                .asString();

                classpath.add(
                        launchConfig.getLibrariesDirectory()
                                .resolve(path)
                                .toAbsolutePath()
                                .toString()
                );
            }

            String classpathString =
                    String.join(
                            separator,
                            classpath
                    );

            LaunchOptions options =
                    LaunchOptions.create()

                            .setVariable(
                                    "auth_player_name",
                                    user == null ? "offlineuser" : user.getUsername()
                            )

                            .setVariable(
                                    "version_name",
                                    version
                            )

                            .setVariable(
                                    "game_directory",
                                    launchConfig.getJarFile().getParent()
                                            .toAbsolutePath()
                                            .toString()
                            )

                            .setVariable(
                                    "assets_root",
                                    launchConfig.getAssetsDirectory()
                                            .toAbsolutePath()
                                            .toString()
                            )

                            .setVariable(
                                    "assets_index_name",
                                    versionJson.get("assetIndex") != null && versionJson.get("assetIndex").asObject().get("id") != null
                                            ? versionJson.get("assetIndex").asObject().get("id").asString()
                                            : ""
                            )

                            .setVariable(
                                    "auth_uuid",
                                    user == null ? "12345678901234567890" : user.getUuid()
                            )

                            .setVariable(
                                    "auth_access_token",
                                    user == null ? "some-access-token" : user.getAccessToken()
                            )

                            .setVariable(
                                    "auth_xuid",
                                    "" // todo find out what this is
                            )

                            .setVariable(
                                    "version_type",
                                    versionJson.get("type") != null ? versionJson.get("type").asString() : "release"
                            )

                            .setVariable(
                                    "resolution_width",
                                    String.valueOf(launchConfig.getResolutionWidth())
                            )

                            .setVariable(
                                    "resolution_height",
                                    String.valueOf(launchConfig.getResolutionHeight())
                            )

                            .setVariable(
                                    "launcher_name",
                                    String.valueOf(launchConfig.getLauncherName())
                            )

                            .setVariable(
                                    "launcher_version",
                                    String.valueOf(launchConfig.getLauncherVersion())
                            )

                            .setVariable(
                                    "classpath",
                                    classpathString
                            )

                            .setVariable(
                                    "natives_directory",
                                    launchConfig.getNativesDirectory() != null ?
                                            launchConfig.getNativesDirectory().toAbsolutePath().toString() :
                                    launchConfig.getJarFile().getParent()
                                            .resolve("natives")
                                            .toAbsolutePath()
                                            .toString()
                            )

                            .setFeature(
                                    "has_custom_resolution",
                                    launchConfig.isCustomResolution()
                            )

                            .setFeature(
                                    "is_demo_user",
                                    launchConfig.isDemoUser()
                            );

            JsonObject arguments =
                    versionJson
                            .get("arguments")
                            .asObject();

            JsonValue defaultUserJvmValue = arguments.get("default-user-jvm");
            JsonArray defaultUserJvm = (defaultUserJvmValue != null) ? defaultUserJvmValue.asArray() : new JsonArray();

            JsonValue jvmValue = arguments.get("jvm");
            JsonArray jvm = (jvmValue != null) ? jvmValue.asArray() : new JsonArray();

            JsonArray mergedJvm =
                    new JsonArray();

            for (JsonValue value : defaultUserJvm) {
                mergedJvm.add(value);
            }

            for (JsonValue value : jvm) {
                mergedJvm.add(value);
            }

            List<String> jvmArgs =
                    JvmArgumentsParser.parse(
                            mergedJvm,
                            launchConfig.getMinRam(),
                            launchConfig.getMaxRam()
                    );

            jvmArgs.addAll(launchConfig.getExtraJvmArgs());

            // Apply variable substitution to JVM arguments
            jvmArgs = X11Helper.substituteVariables(jvmArgs, options.getVariables());

            JsonValue gameValue = arguments.get("game");
            JsonArray gameArray = (gameValue != null) ? gameValue.asArray() : new JsonArray();

            List<String> gameArgs =
                    GameArgumentsParser.parse(
                            gameArray,

                            options.getVariables(),
                            options.getFeatures()
                    );

            ArrayList<String> command =
                    new ArrayList<>();

            command.add(
                    javaInstallation
                            .executable()
                            .toAbsolutePath()
                            .toString()
            );

            command.addAll(jvmArgs);

            String mainClass = versionJson.get("mainClass") != null ? versionJson.get("mainClass").asString() : "";
            if (mainClass.isEmpty()) {
                throw new RuntimeException("Main class not found in version JSON");
            }

            command.add(mainClass);

            command.addAll(gameArgs);

            ProcessBuilder processBuilder =
                    new ProcessBuilder(command);

            X11Helper.configureGraphicsEnvironment(processBuilder);


            System.out.println(
                    String.join(" ", command)
            );

            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Minecraft exited with code " + exitCode);
            }

        } catch (HttpException | IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (NoConnectionException e) {
            System.err.println("You do not have a internet connection");
        }
    }
}