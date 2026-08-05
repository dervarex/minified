package com.dervarex.minified.launch.profile;

import com.dervarex.minified.launch.exceptions.loader.UnknownLoaderTypeException;
import com.dervarex.minified.launch.exceptions.profile.FailedToLoadProfileException;
import com.dervarex.minified.launch.exceptions.profile.FailedToSaveProfileException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.custom.CustomLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class ProfileFactory {
    private static final Map<String, Function<JsonObject, Loader>> LOADERS = Map.of(
            "VANILLA", ProfileFactory::readVanillaLoader,
            "FABRIC", ProfileFactory::readFabricLoader,
            "FORGE", ProfileFactory::readForgeLoader,
            "QUILT", ProfileFactory::readQuiltLoader,
            "NEOFORGE", ProfileFactory::readNeoforgeLoader,
            "CUSTOM", ProfileFactory::readCustomLoader
    );

    /**
     * Save a LaunchConfiguration to the specified path as a JSON file
     * @param profile the LaunchConfiguration that should be saved
     * @param path the full path to the JSON file where it should be saved to
     */
    public static void save(LaunchConfiguration profile, Path path) {
        JsonFile profileJson = new JsonFile();
        JsonObject root = profileJson.asObject();

        // Memory
        root.put("minRam", profile.getMinRam());
        root.put("maxRam", profile.getMaxRam());

        // Downloads
        root.put("downloadThreads", profile.getDownloadThreads());

        // Resolution
        JsonObject resolution = new JsonObject();
        resolution.put("width", profile.getResolutionWidth());
        resolution.put("height", profile.getResolutionHeight());
        root.put("resolution", resolution);

        // Launcher metadata
        JsonObject launcher = new JsonObject();
        launcher.put("name", profile.getLauncherName());
        launcher.put("version", profile.getLauncherVersion());
        root.put("launcher", launcher);

        // Flags
        JsonObject flags = new JsonObject();
        flags.put("demoUser", profile.isDemoUser());
        flags.put("customResolution", profile.isCustomResolution());
        root.put("flags", flags);

        // Paths
        JsonObject paths = new JsonObject();
        paths.put("jarFile", profile.getJarFile().toString());
        paths.put("librariesDirectory", profile.getLibrariesDirectory().toString());
        paths.put("assetsDirectory", profile.getAssetsDirectory().toString());

        if (profile.getNativesDirectory() != null) {
            paths.put("nativesDirectory", profile.getNativesDirectory().toString());
        }

        if (profile.getCustomJavaExecutable() != null) {
            paths.put("customJavaExecutable", profile.getCustomJavaExecutable().toString());
        }

        root.put("paths", paths);

        // Launch options
        JsonObject launch = new JsonObject();

        JsonArray jvmArgs = new JsonArray();
        for (String arg : profile.getExtraJvmArgs()) {
            jvmArgs.add(arg);
        }
        launch.put("extraJvmArgs", jvmArgs);

        if (profile.getLoader() != null) {
            launch.put("loader", serializeLoader(profile.getLoader()));
        }

        root.put("launch", launch);

        // User
        JsonObject user = new JsonObject();
        user.put("offlineUsername", profile.getOfflineUsername());
        root.put("user", user);

        try {
            profileJson.save(path);
        } catch (IOException e) {
            throw new FailedToSaveProfileException("Failed to save profile", e);
        }
    }

    /**
     * Load a LaunchConfiguration from a specific path
     * @param path the path of the JSON file that was saved by {@link #save(LaunchConfiguration, Path)}
     * @return the LaunchConfiguration that can be used to launch the game
     */
    public static LaunchConfiguration load(Path path) {
        JsonFile profileJson;
        try {
            profileJson = new JsonFile(path);
        } catch (IOException e) {
            throw new FailedToLoadProfileException("Failed to load profile", e);
        }

        JsonObject root = profileJson.asObject();

        JsonObject resolution = root.getObject("resolution");
        JsonObject launcher = root.getObject("launcher");
        JsonObject flags = root.getObject("flags");
        JsonObject paths = root.getObject("paths");
        JsonObject launch = root.getObject("launch");
        JsonObject user = root.getObject("user");

        Loader loader = null;
        if (launch.has("loader")) {
            loader = deserializeLoader(launch.get("loader").asObject());
        }

        LaunchConfiguration.Builder builder = new LaunchConfiguration.Builder()
                .minRam(root.get("minRam").asInt())
                .maxRam(root.get("maxRam").asInt())
                .downloadThreads(root.get("downloadThreads").asInt())
                .launcherName(launcher.get("name").asString())
                .launcherVersion(launcher.get("version").asString())
                .isDemoUser(flags.get("demoUser").asBoolean())
                .offlineUsername(user.get("offlineUsername").asString());

        if (flags.get("customResolution").asBoolean()) {
            builder.resolution(
                    resolution.get("width").asInt(),
                    resolution.get("height").asInt()
            );
        }

        if (paths.has("jarFile")) {
            builder.jarFile(Path.of(paths.get("jarFile").asString()));
        }

        if (paths.has("librariesDirectory")) {
            builder.librariesDirectory(Path.of(paths.get("librariesDirectory").asString()));
        }

        if (paths.has("assetsDirectory")) {
            builder.assetsDirectory(Path.of(paths.get("assetsDirectory").asString()));
        }

        if (paths.has("nativesDirectory")) {
            builder.nativesDirectory(Path.of(paths.get("nativesDirectory").asString()));
        }

        if (paths.has("customJavaExecutable")) {
            builder.customJavaExecutable(Path.of(paths.get("customJavaExecutable").asString()));
        }

        if (launch.has("extraJvmArgs")) {
            builder.extraJvmArgs(
                    launch.getArray("extraJvmArgs")
                            .values()
                            .stream()
                            .map(v -> v.asString())
                            .toList()
            );
        }

        if (loader != null) {
            builder.loader(loader);
        }

        return builder.build();
    }

    private static JsonObject serializeLoader(Loader loader) {
        JsonObject json = new JsonObject();

        if (loader instanceof CustomLoader custom) {
            json.put("type", "CUSTOM");
            json.put("name", custom.name());
            json.put("mcVersion", custom.mcVersion());
            json.put("loaderVersion", custom.loaderVersion());
            json.put("iconUrl", custom.iconUrl());
            json.put("mainClass", custom.mainClass());

            JsonArray jvmArgs = new JsonArray();
            for (String arg : custom.customJvmArgs()) {
                jvmArgs.add(arg);
            }
            json.put("customJvmArgs", jvmArgs);

            JsonArray gameArgs = new JsonArray();
            for (String arg : custom.customGameArgs()) {
                gameArgs.add(arg);
            }
            json.put("customGameArgs", gameArgs);

            JsonArray classpathEntries = new JsonArray();
            for (String entry : custom.customClasspathEntries()) {
                classpathEntries.add(entry);
            }
            json.put("customClasspathEntries", classpathEntries);

            return json;
        }

        json.put("type", loader.name());
        json.put("mcVersion", loader.mcVersion());
        json.put("loaderVersion", loader.loaderVersion());
        json.put("iconUrl", loader.iconUrl());
        return json;
    }

    private static Loader deserializeLoader(JsonObject loaderJson) {
        String type = loaderJson.get("type").asString().toUpperCase(Locale.ROOT);
        Function<JsonObject, Loader> loaderFactory = LOADERS.get(type);

        if (loaderFactory == null) {
            throw new UnknownLoaderTypeException("Unknown loader type: " + type);
        }

        return loaderFactory.apply(loaderJson);
    }

    private static Loader readVanillaLoader(JsonObject loaderJson) {
        return new VanillaLoader(loaderJson.get("mcVersion").asString());
    }

    private static Loader readFabricLoader(JsonObject loaderJson) {
        return new FabricLoader(
                loaderJson.get("mcVersion").asString(),
                loaderJson.get("loaderVersion").asString()
        );
    }

    private static Loader readForgeLoader(JsonObject loaderJson) {
        return new ForgeLoader(
                loaderJson.get("mcVersion").asString(),
                loaderJson.get("loaderVersion").asString()
        );
    }

    private static Loader readQuiltLoader(JsonObject loaderJson) {
        return new QuiltLoader(
                loaderJson.get("mcVersion").asString(),
                loaderJson.get("loaderVersion").asString()
        );
    }

    private static Loader readNeoforgeLoader(JsonObject loaderJson) {
        return new NeoforgeLoader(
                loaderJson.get("mcVersion").asString(),
                loaderJson.get("loaderVersion").asString()
        );
    }

    private static Loader readCustomLoader(JsonObject loaderJson) {
        return new CustomLoader(
                loaderJson.get("name").asString(),
                loaderJson.get("mcVersion").asString(),
                loaderJson.get("loaderVersion").asString(),
                loaderJson.get("iconUrl").asString(),
                loaderJson.get("mainClass").asString(),
                readStringList(loaderJson.getArray("customJvmArgs")),
                readStringList(loaderJson.getArray("customGameArgs")),
                readStringList(loaderJson.getArray("customClasspathEntries"))
        );
    }

    private static List<String> readStringList(JsonArray array) {
        return array.values()
                .stream()
                .map(v -> v.asString())
                .toList();
    }
}