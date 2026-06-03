package com.dervarex.minified.launch.launch;

import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionFetcher;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionJson;
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

    static String buildClasspath(
            JsonFile versionJson,
            LaunchConfigurator config
    ) {
        String separator =
                System.getProperty("os.name")
                        .toLowerCase()
                        .contains("win")
                        ? ";"
                        : ":";

        ArrayList<String> classpath =
                new ArrayList<>();

        classpath.add(
                config.getJarFile()
                        .toAbsolutePath()
                        .toString()
        );

        JsonArray libraries =
                versionJson
                        .get("libraries")
                        .asArray();

        for (JsonValue value : libraries) {
            addLibrary(
                    value.asObject(),
                    classpath,
                    config
            );
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

            case Forge:
                try {
                    JsonObject forgeProfile =
                            ForgeVersionJson.getVersionJson(
                                    config.getJarFile().getParent(),
                                    new ForgeVersionFetcher().getLatest(versionJson.get("id").asString())
                            ).asObject();

                    addModLoaderLibraries(
                            forgeProfile.get("libraries").asArray(),
                            classpath,
                            config
                    );
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to load Forge libraries",
                            e
                    );
                }
                break;
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
}