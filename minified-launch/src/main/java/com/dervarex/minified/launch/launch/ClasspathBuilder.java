package com.dervarex.minified.launch.launch;

import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionFetcher;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionJson;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionFetcher;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionJson;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ClasspathBuilder {

    static String buildClasspath(JsonFile versionJson, LaunchConfigurator config) {
        String separator = System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ":";

        ArrayList<String> classpath = new ArrayList<>();

//        if (config.getLoader() == Loader.NeoForge) {
//            String neoForgeVersion = new NeoVersionFetcher().getLatest(versionJson.get("id").asString());
//            classpath.add(
//                    config.getLibrariesDirectory()
//                            .resolve("net/neoforged/minecraft-client-patched")
//                            .resolve(neoForgeVersion)
//                            .resolve("minecraft-client-patched-" + neoForgeVersion + ".jar")
//                            .toAbsolutePath()
//                            .toString()
//            );
//        } else {
//            classpath.add(config.getJarFile().toAbsolutePath().toString());
//        }
        classpath.add(
                config.getJarFile()
                        .toAbsolutePath()
                        .toString()
        );

        JsonArray libraries = versionJson.get("libraries").asArray();
        for (JsonValue value : libraries) {
            addLibrary(value.asObject(), classpath, config);
        }

        switch (config.getLoader()) {
            case Vanilla:
                break;

            case Fabric:
                try {
                    JsonObject fabricProfile =
                            FabricLoaderFetcher.getLatestProfile(
                                    versionJson.get("id").asString()
                            );

                    addModLoaderLibraries(
                            fabricProfile.get("libraries").asArray(),
                            classpath,
                            config
                    );
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to load Fabric libraries",
                            e
                    );
                }
                break;

            case Quilt:
                try {
                    JsonObject quiltProfile =
                            QuiltLoaderFetcher.getLatestProfile(
                                    versionJson.get("id").asString()
                            );

                    addModLoaderLibraries(
                            quiltProfile.get("libraries").asArray(),
                            classpath,
                            config
                    );
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to load Quilt libraries",
                            e
                    );
                }
                break;
            case NeoForge:
                try {
                    JsonObject neoForgeProfile =
                            NeoVersionJson.getVersionJson(
                                    config.getJarFile().getParent(),
                                    new NeoVersionFetcher().getLatest(versionJson.get("id").asString())
                            ).asObject();

                    addModLoaderLibraries(
                            neoForgeProfile.get("libraries").asArray(),
                            classpath,
                            config
                    );
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to load NeoForge libraries",
                            e
                    );
                }
                break;
//            case NeoForge:
//                try {
//                    String neoForgeVersion =
//                            new NeoVersionFetcher().getLatest(versionJson.get("id").asString());
//
//                    ensureNeoForgeCommonJar(
//                            config,
//                            versionJson.get("id").asString(),
//                            neoForgeVersion
//                    );
//
//                    // Das Runtime-Jar, das FML erwartet
//                    classpath.add(
//                            config.getLibrariesDirectory()
//                                    .resolve("net/minecraft/client")
//                                    .resolve(versionJson.get("id").asString() + "-1")
//                                    .resolve("client-" + versionJson.get("id").asString() + "-1-srg.jar")
//                                    .toAbsolutePath()
//                                    .toString()
//                    );
//
//                    JsonObject neoForgeProfile =
//                            NeoVersionJson.getVersionJson(
//                                    config.getJarFile().getParent(),
//                                    neoForgeVersion
//                            ).asObject();
//
//                    for (JsonValue value : neoForgeProfile.get("libraries").asArray()) {
//                        JsonObject lib = value.asObject();
//
//                        if (isNeoForgePatchedClient(lib)) {
//                            continue;
//                        }
//
//                        addLibrary(lib, classpath, config);
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException("Failed to load NeoForge libraries", e);
//                }
//                break;
//            case NeoForge:
//                try {
//                    String neoForgeVersion = new NeoVersionFetcher().getLatest(versionJson.get("id").asString());
//
//                    JsonObject neoForgeProfile =
//                            NeoVersionJson.getVersionJson(
//                                    config.getJarFile().getParent(),
//                                    neoForgeVersion
//                            ).asObject();
//
//                    // add the universal jar
//                    classpath.add(
//                            config.getLibrariesDirectory()
//                                    .resolve("net/neoforged/neoforge")
//                                    .resolve(neoForgeVersion)
//                                    .resolve("neoforge-" + neoForgeVersion + "-universal.jar")
//                                    .toAbsolutePath()
//                                    .toString()
//                    );
//
//                    addModLoaderLibraries(
//                            neoForgeProfile.get("libraries").asArray(),
//                            classpath,
//                            config
//                    );
//                } catch (Exception e) {
//                    throw new RuntimeException("Failed to load NeoForge libraries", e);
//                }
//                break;
//            case NeoForge:
//                try {
//                    JsonObject neoForgeProfile =
//                            NeoVersionJson.getVersionJson(
//                                    config.getJarFile().getParent(),
//                                    new NeoVersionFetcher().getLatest(versionJson.get("id").asString())
//                            ).asObject();
//
//                    addModLoaderLibraries(
//                            neoForgeProfile.get("libraries").asArray(),
//                            classpath,
//                            config
//                    );
//                } catch (Exception e) {
//                    throw new RuntimeException(
//                            "Failed to load NeoForge libraries",
//                            e
//                    );
//                }
//                break;
        }

        return String.join(
                separator,
                classpath
        );
    }

    private static void addLibrary(
            JsonObject library,
            ArrayList<String> classpath,
            LaunchConfigurator config
    ) {
        JsonValue downloadsValue =
                library.get("downloads");

        if (downloadsValue != null) {
            JsonObject downloads =
                    downloadsValue.asObject();

            JsonValue artifactValue =
                    downloads.get("artifact");

            if (artifactValue != null) {
                JsonObject artifact =
                        artifactValue.asObject();

                JsonValue pathValue =
                        artifact.get("path");

                if (pathValue != null) {
                    String path =
                            pathValue.asString();

                    classpath.add(
                            resolveLibraryPath(
                                    config,
                                    path
                            )
                    );
                    return;
                }
            }
        }

        JsonValue nameValue =
                library.get("name");

        if (nameValue == null) {
            return;
        }

        String[] parts =
                nameValue.asString()
                        .split(":");

        if (parts.length < 3) {
            return;
        }

        String path = getPathForLibrary(parts);

        classpath.add(
                resolveLibraryPath(
                        config,
                        path
                )
        );
    }

    private static void addModLoaderLibraries(
            JsonArray libraries,
            ArrayList<String> classpath,
            LaunchConfigurator config
    ) {
        for (JsonValue value : libraries) {
            addLibrary(
                    value.asObject(),
                    classpath,
                    config
            );
        }
    }

    private static String resolveLibraryPath(
            LaunchConfigurator config,
            String relativePath
    ) {
        Path primary =
                config.getLibrariesDirectory()
                        .resolve(relativePath);

        Path secondary =
                config.getJarFile()
                        .getParent()
                        .resolve("libraries")
                        .resolve(relativePath);

        if (Files.exists(secondary)) {
            return secondary.toAbsolutePath().toString();
        }

        if (Files.exists(primary)) {
            return primary.toAbsolutePath().toString();
        }

        return primary.toAbsolutePath().toString();
    }

    private static @NotNull String getPathForLibrary(String[] parts) {
        String groupId =
                parts[0];

        String artifactId =
                parts[1];

        String version =
                parts[2];

        String fileName =
                artifactId + "-" + version;

        if (parts.length > 3) {
            fileName += "-" + parts[3];
        }

        fileName += ".jar";

        return groupId.replace('.', '/')
                + "/"
                + artifactId
                + "/"
                + version
                + "/"
                + fileName;
    }
    private static boolean isNeoForgePatchedClient(JsonObject library) {
        JsonValue nameValue = library.get("name");
        if (nameValue != null && nameValue.asString().startsWith("net.neoforged:minecraft-client-patched:")) {
            return true;
        }

        JsonValue downloadsValue = library.get("downloads");
        if (downloadsValue != null) {
            JsonValue artifactValue = downloadsValue.asObject().get("artifact");
            if (artifactValue != null) {
                JsonValue pathValue = artifactValue.asObject().get("path");
                if (pathValue != null) {
                    return pathValue.asString().startsWith("net/neoforged/minecraft-client-patched/");
                }
            }
        }

        return false;
    }
}