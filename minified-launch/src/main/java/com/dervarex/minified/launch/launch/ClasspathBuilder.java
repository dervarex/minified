package com.dervarex.minified.launch.launch;

import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;

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
                    config.getLibrariesDirectory()
                            .resolve(path)
                            .toAbsolutePath()
                            .toString()
            );
        }
        switch (config.getLoader()) {
            case Vanilla:
                // Nothing additional required
                break;
            case Fabric:
                try {
                    JsonObject fabricProfile =
                            FabricLoaderFetcher.getLatestProfile(
                                    versionJson.get("id").asString()
                            );

                    JsonArray fabricLibraries =
                            fabricProfile
                                    .get("libraries")
                                    .asArray();

                    for (JsonValue value : fabricLibraries) {

                        JsonObject library =
                                value.asObject();

                        String[] parts =
                                library.get("name")
                                        .asString()
                                        .split(":");

                        if (parts.length != 3) {
                            continue;
                        }

                        String groupId =
                                parts[0];

                        String artifactId =
                                parts[1];

                        String version =
                                parts[2];

                        String path =
                                groupId.replace('.', '/')
                                        + "/"
                                        + artifactId
                                        + "/"
                                        + version
                                        + "/"
                                        + artifactId
                                        + "-"
                                        + version
                                        + ".jar";

                        classpath.add(
                                config.getLibrariesDirectory()
                                        .resolve(path)
                                        .toAbsolutePath()
                                        .toString()
                        );
                    }

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

                    JsonArray quiltLibraries =
                            quiltProfile
                                    .get("libraries")
                                    .asArray();

                    for (JsonValue value : quiltLibraries) {

                        JsonObject library =
                                value.asObject();

                        String[] parts =
                                library.get("name")
                                        .asString()
                                        .split(":");

                        if (parts.length != 3) {
                            continue;
                        }

                        String groupId =
                                parts[0];

                        String artifactId =
                                parts[1];

                        String version =
                                parts[2];

                        String path =
                                groupId.replace('.', '/')
                                        + "/"
                                        + artifactId
                                        + "/"
                                        + version
                                        + "/"
                                        + artifactId
                                        + "-"
                                        + version
                                        + ".jar";

                        classpath.add(
                                config.getLibrariesDirectory()
                                        .resolve(path)
                                        .toAbsolutePath()
                                        .toString()
                        );
                    }

                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to load Quilt libraries",
                            e
                    );
                }

        }

        return
                String.join(
                        separator,
                        classpath
                );
    }
}