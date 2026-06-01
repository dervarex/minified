package com.dervarex.minified.launch;

import com.dervarex.minified.auth.User;
import com.dervarex.minified.launch.arguments.GameArgumentsParser;
import com.dervarex.minified.launch.arguments.JvmArgumentsParser;
import com.dervarex.minified.launch.download.ClientDownloader;
import com.dervarex.minified.launch.download.assets.AssetDownloader;
import com.dervarex.minified.launch.download.libraries.LibraryDownloader;
//import com.dervarex.minified.launch.patch.JarPatcher;
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
import java.util.Objects;
import java.util.List;
import java.util.Map;

public class Launcher {
    /**
     * Downloads required files and launches Minecraft.
     *
     * @param version the Minecraft version to launch
     * @param jarFile the full path to the client JAR file
     *                (for example: /path/to/client.jar)
     * @param librariesDirectory the directory containing libraries
     * @param assetsDirectory the directory containing assets
     * @param minRam minimum RAM allocated to the JVM, in megabytes
     * @param maxRam maximum RAM allocated to the JVM, in megabytes
     * @param launchConfig configuration used by the launcher when starting the game,
     *                     such as the number of download threads, launcher name,
     *                     and launcher version
     *<p>
     *                     Example:
     *                     <pre>{@code
     * LaunchConfigurator config = new LaunchConfigurator.Builder()
     *     .downloadThreads(10)
     *     .launcherName("MinifiedLauncher")
     *     .launcherVersion("1.0.0")
     *     .build();
     * }</pre>
     */
    public static void launchMinecraft(
            String version,
            Path jarFile,
            Path librariesDirectory,
            Path assetsDirectory,
            int minRam,
            int maxRam,
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

            // Downloads (will skip files if they already exist)
            LibraryDownloader libDownloader = new LibraryDownloader(launchConfig.getDownloadThreads());
            AssetDownloader assetDownloader = new AssetDownloader(launchConfig.getDownloadThreads());
            ClientDownloader clientDownloader = new ClientDownloader();

            libDownloader.downloadLibraries(version, librariesDirectory);
            assetDownloader.downloadAssets(version, assetsDirectory);
            clientDownloader.downloadClient(version, jarFile);

            String separator =
                    System.getProperty("os.name")
                            .toLowerCase()
                            .contains("win")
                            ? ";"
                            : ":";

            ArrayList<String> classpath =
                    new ArrayList<>();

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
                        librariesDirectory
                                .resolve(path)
                                .toAbsolutePath()
                                .toString()
                );
            }

            classpath.add(
                    jarFile
                            .toAbsolutePath()
                            .toString()
            );

            String classpathString =
                    String.join(
                            separator,
                            classpath
                    );

            LaunchOptions options =
                    LaunchOptions.create()

                            .setVariable(
                                    "auth_player_name",
                                    user.getUsername()
                            )

                            .setVariable(
                                    "version_name",
                                    version
                            )

                            .setVariable(
                                    "game_directory",
                                    jarFile.getParent()
                                            .toAbsolutePath()
                                            .toString()
                            )

                            .setVariable(
                                    "assets_root",
                                    assetsDirectory
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
                                    user.getUuid()
                            )

                            .setVariable(
                                    "auth_access_token",
                                    user.getAccessToken()
                            )

                            /*.setVariable(
                                    "clientid",
                                    "" // todo find out what this is
                            )*/

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
                                    jarFile.getParent()
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
                            minRam,
                            maxRam
                    );

            // Apply variable substitution to JVM arguments
            jvmArgs = substituteVariables(jvmArgs, options.getVariables());

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

            command.add("java");

            command.addAll(jvmArgs);

            String mainClass = versionJson.get("mainClass") != null ? versionJson.get("mainClass").asString() : "";
            if (mainClass.isEmpty()) {
                throw new RuntimeException("Main class not found in version JSON");
            }

            command.add(mainClass);

            command.addAll(gameArgs);

            ProcessBuilder processBuilder =
                    new ProcessBuilder(command);

            configureGraphicsEnvironment(processBuilder);


            System.out.println(
                    String.join(" ", command)
            );

            /*
            // Attempt to patch the JAR file
            try {
                File jarFileObj = jarFile.toFile();
                File tempPatchedJar = new File(jarFileObj.getParent(), jarFileObj.getName() + ".patched");
                JarPatcher.patch(jarFileObj, tempPatchedJar);
                // If patching succeeded, replace original with patched version
                if (tempPatchedJar.exists()) {
                    jarFileObj.delete();
                    tempPatchedJar.renameTo(jarFileObj);
                }
            } catch (Exception e) {
                // Log but don't fail if patching fails - the JAR may still be usable
                System.err.println("Warning: JAR patching failed, continuing without patch: " + e.getMessage());
            }
            */

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

    // Normalizes the child JVM graphics environment so Linux launches can
    // fall back to X11 when a local Xwayland display is available.
    static void configureGraphicsEnvironment(
            ProcessBuilder processBuilder
    ) {
        configureGraphicsEnvironment(
                processBuilder,
                Path.of("/tmp/.X11-unix")
        );
    }

    // Normalizes the child JVM graphics environment using the supplied X11
    // socket directory.
    static void configureGraphicsEnvironment(
            ProcessBuilder processBuilder,
            Path x11SocketDirectory
    ) {
        Map<String, String> environment =
                processBuilder.environment();

        String display = environment.get("DISPLAY");

        if (display != null && !display.isBlank()) {
            return;
        }

        String resolvedDisplay = resolveDisplay(x11SocketDirectory);

        if (resolvedDisplay == null) {
            return;
        }

        environment.put("DISPLAY", resolvedDisplay);

        String waylandDisplay = environment.get("WAYLAND_DISPLAY");

        if (waylandDisplay != null && !waylandDisplay.isBlank()) {
            environment.remove("WAYLAND_DISPLAY");
            environment.put("XDG_SESSION_TYPE", "x11");
        }

        System.out.println(
                "Detected X11 display " + resolvedDisplay +
                        " for Minecraft"
        );
    }

    // Attempts to infer an active X11 display from socket names such as X0,
    // X1, and so on.
    static String resolveDisplay(
            Path x11SocketDirectory
    ) {
        if (x11SocketDirectory == null || !Files.isDirectory(x11SocketDirectory)) {
            return null;
        }

        try (var sockets = Files.list(x11SocketDirectory)) {
            return sockets
                    .map(path -> parseDisplayNumber(
                            path.getFileName().toString()
                    ))
                    .filter(Objects::nonNull)
                    .sorted()
                    .findFirst()
                    .map(number -> ":" + number)
                    .orElse(null);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static Integer parseDisplayNumber(
            String socketName
    ) {
        if (!socketName.startsWith("X")) {
            return null;
        }

        try {
            return Integer.parseInt(socketName.substring(1));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Replaces ${key} placeholders in the supplied arguments.
     */
    private static List<String> substituteVariables(
            List<String> arguments,
            Map<String, String> variables
    ) {
        ArrayList<String> result = new ArrayList<>();
        for (String arg : arguments) {
            String substituted = arg;
            for (String key : variables.keySet()) {
                String value = variables.get(key);
                if (value == null) {
                    value = "";
                }
                substituted = substituted.replace("${" + key + "}", value);
            }
            result.add(substituted);
        }
        return result;
    }
}
// todo: launch in offline(cracked) mode
// java <jvm args> <main class> <mc args>
//todo: clean up + overloading