package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.launch.exceptions.libraries.FailedToLoadLibrariesException;
import com.dervarex.minified.launch.exceptions.loader.UnexpectedLoaderException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionFetcher;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionJson;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.launch.utils.OSUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import org.apiguardian.api.API;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
public class ClasspathBuilder {
    public static String buildClasspath(JsonFile versionJson, LaunchConfiguration config) {
        String separator = System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ":";

        ArrayList<String> classpath = new ArrayList<>();

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
            case VanillaLoader ignored:
                break;

            case FabricLoader ignored:
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
                    throw new FailedToLoadLibrariesException(
                            "Failed to load Fabric libraries",
                            config.getLoader(),
                            e
                    );
                }
                break;

            case QuiltLoader ignored:
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
                    throw new FailedToLoadLibrariesException(
                            "Failed to load Quilt libraries",
                            config.getLoader(),
                            e
                    );
                }
                break;
            case NeoforgeLoader ignored:
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
                    throw new FailedToLoadLibrariesException(
                            "Failed to load NeoForge libraries",
                            config.getLoader(),
                            e
                    );
                }
                break;
            case ForgeLoader ignored:
                break;
            default:
                throw new UnexpectedLoaderException("Unexpected loader: " + config.getLoader());
        }

        return String.join(
                separator,
                classpath
        );
    }

    private static void addLibrary(
            JsonObject library,
            ArrayList<String> classpath,
            LaunchConfiguration config
    ) {
        if (!isAllowed(library)) {
            return;
        }

        JsonValue nativesValue =
                library.get("natives");

        if (nativesValue != null) {
            return;
        }

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
            LaunchConfiguration config
    ) {
        for (JsonValue value : libraries) {
            addLibrary(
                    value.asObject(),
                    classpath,
                    config
            );
        }
    }

    private static boolean isAllowed(JsonObject library) {
        if (!library.has("rules")) {
            return true;
        }

        JsonArray rules = library.get("rules").asArray();
        String os = OSUtil.getMinecraftOs();

        boolean allowed = false;

        for (JsonValue ruleValue : rules) {
            JsonObject rule = ruleValue.asObject();
            String action = rule.get("action").asString();

            if (!rule.has("os")) {
                allowed = action.equals("allow");
                continue;
            }

            JsonObject osObject = rule.get("os").asObject();
            String ruleOs = osObject.get("name").asString();

            if (ruleOs.equals(os)) {
                allowed = action.equals("allow");
            }
        }

        return allowed;
    }
    private static String resolveLibraryPath(
            LaunchConfiguration config,
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

//        if (Files.exists(primary)) {
//            return primary.toAbsolutePath().toString();
//        } // who wrote this shit, that if doesn't do anything good
        // oh I'm the only dev

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