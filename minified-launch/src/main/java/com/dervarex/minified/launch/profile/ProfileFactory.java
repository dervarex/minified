package com.dervarex.minified.launch.profile;

import com.dervarex.minified.launch.exceptions.loader.UnexpectedLoaderException;
import com.dervarex.minified.launch.exceptions.profile.FailedToLoadProfileException;
import com.dervarex.minified.launch.exceptions.profile.FailedToSaveProfileException;
import com.dervarex.minified.launch.exceptions.profile.UnsupportedProfileFormatException;
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
import com.dervarex.minified.utils.json.JsonValue;
import org.apiguardian.api.API;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * JSON storage for profiles
 * <p>
 * Format versions:
 * <ul>
 *     <li>1: only the launch configuration, no {@code formatVersion} field</li>
 *     <li>2: adds {@code formatVersion}, the {@code profile} metadata, the {@code files} list and the options
 *     fullscreen, extraGameArgs, environment and serverAddress</li>
 * </ul>
 */
@API(status = API.Status.STABLE)
public class ProfileFactory {

    private ProfileFactory() {}
    /**
     * The format version, gets changed if anything is incompatible with an older version
     *
     * Format versions:
     * 2: >=Minified v4.0.0
     */
    public static final int FORMAT_VERSION = 2;

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
    @API(status = API.Status.STABLE)
    public static void save(LaunchConfiguration profile, Path path) {
        write(serialize(null, profile, List.of(), true), path);
    }

    /**
     * Save a profile with its metadata and files to the specified path as a JSON file
     * @param profile the profile that should be saved
     * @param path the full path to the JSON file where it should be saved to
     */
    @API(status = API.Status.STABLE)
    public static void save(Profile profile, Path path) {
        write(serialize(profile.metadata(), profile.configuration(), profile.files(), true), path);
    }

    /**
     * Save a profile so it can be given to someone else, the paths and the offline username are left out,
     * because they only make sense on this pc. Load it with {@link #loadProfile(Path, ProfileEnvironment)}
     * and download the files with {@link ProfileInstaller#install(Profile)}
     * @param profile the profile that should be shared
     * @param path the full path to the JSON file where it should be saved to
     */
    @API(status = API.Status.STABLE)
    public static void export(Profile profile, Path path) {
        write(serialize(profile.metadata(), profile.configuration(), profile.files(), false), path);
    }

    /**
     * Load a LaunchConfiguration
     * @param path the path of the JSON file that was saved by {@link #save(LaunchConfiguration, Path)}
     * @return the LaunchConfiguration that can be used to launch the game
     */
    @API(status = API.Status.STABLE)
    public static LaunchConfiguration load(Path path) {
        return loadProfile(path).configuration();
    }

    /**
     * Load a profile that was saved with its local paths
     * @param path the path of the JSON file that was saved by {@link #save(Profile, Path)} or {@link #save(LaunchConfiguration, Path)}
     * @return the profile, files without a {@code profile} section get new metadata with a random id
     */
    @API(status = API.Status.STABLE)
    public static Profile loadProfile(Path path) {
        return loadProfile(path, null);
    }

    /**
     * Load a profile and fill in the for this pc specific values, needed for profiles from {@link #export(Profile, Path)}
     * @param path the path of the JSON file
     * @param environment the local paths. May be null
     * @return the profile
     */
    @API(status = API.Status.STABLE)
    public static Profile loadProfile(Path path, ProfileEnvironment environment) {
        try {
            JsonFile profileJson = new JsonFile(path);
            JsonObject root = profileJson.asObject();

            int formatVersion = root.has("formatVersion") ? root.get("formatVersion").asInt() : 1;
            if (formatVersion > FORMAT_VERSION) {
                throw new UnsupportedProfileFormatException(
                        "Profile format " + formatVersion + " is newer than the supported format " + FORMAT_VERSION,
                        formatVersion
                );
            }

            JsonObject resolution = Objects.requireNonNull(root.getObject("resolution"), "Missing 'resolution' section");
            JsonObject launcher = Objects.requireNonNull(root.getObject("launcher"), "Missing 'launcher' section");
            JsonObject flags = Objects.requireNonNull(root.getObject("flags"), "Missing 'flags' section");
            JsonObject launch = Objects.requireNonNull(root.getObject("launch"), "Missing 'launch' section");
            JsonObject paths = root.getObject("paths");
            JsonObject user = root.getObject("user");

            if (paths == null && environment == null) {
                throw new FailedToLoadProfileException(
                        "Profile has no paths, it was probably exported for sharing. Load it with a ProfileEnvironment"
                );
            }

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
                    .isDemoUser(flags.get("demoUser").asBoolean());

            if (user != null) {
                builder.offlineUsername(user.get("offlineUsername").asString());
            }

            if (flags.get("customResolution").asBoolean()) {
                builder.resolution(
                        resolution.get("width").asInt(),
                        resolution.get("height").asInt()
                );
            }

            if (flags.has("fullscreen")) {
                builder.fullscreen(flags.get("fullscreen").asBoolean());
            }

            if (paths != null) {
                applyPaths(paths, builder);
            }

            if (environment != null) {
                applyEnvironment(environment, builder);
            }

            if (launch.has("extraJvmArgs")) {
                builder.extraJvmArgs(readStringList(launch.getArray("extraJvmArgs")));
            }

            if (launch.has("extraGameArgs")) {
                builder.extraGameArgs(readStringList(launch.getArray("extraGameArgs")));
            }

            if (launch.has("captureGameOutput")) {
                builder.captureGameOutput(launch.get("captureGameOutput").asBoolean());
            }

            JsonObject environmentVariables = launch.getObject("environment");
            if (environmentVariables != null) {
                for (String name : environmentVariables.keys()) {
                    builder.environmentVariable(name, environmentVariables.getString(name));
                }
            }

            builder.serverAddress(launch.getString("serverAddress"));

            if (loader != null) {
                builder.loader(loader);
            }

            JsonObject metadata = root.getObject("profile");
            return new Profile(
                    metadata != null ? readMetadata(metadata) : ProfileMetadata.builder().build(),
                    builder.build(),
                    readFiles(root.getArray("files"))
            );
        } catch (UnexpectedLoaderException | FailedToLoadProfileException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new FailedToLoadProfileException("Failed to load profile", e);
        }
    }

    private static void write(JsonFile profileJson, Path path) {
        try {
            profileJson.save(path);
        } catch (IOException e) {
            throw new FailedToSaveProfileException("Failed to save profile", e);
        }
    }

    private static JsonFile serialize(
            ProfileMetadata metadata,
            LaunchConfiguration profile,
            List<ProfileFile> files,
            boolean includeLocal
    ) {
        JsonFile profileJson = new JsonFile();
        JsonObject root = profileJson.asObject();

        root.put("formatVersion", FORMAT_VERSION);

        if (metadata != null) {
            root.put("profile", serializeMetadata(metadata));
        }

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
        flags.put("fullscreen", profile.isFullscreen());
        root.put("flags", flags);

        // Paths
        if (includeLocal) {
            JsonObject paths = new JsonObject();
            paths.put("jarFile", profile.getJarFile().toString());
            paths.put("librariesDirectory", profile.getLibrariesDirectory().toString());
            paths.put("assetsDirectory", profile.getAssetsDirectory().toString());

            if (profile.getNativesDirectory() != null) {
                paths.put("nativesDirectory", profile.getNativesDirectory().toString());
            }

            if (profile.getGameDirectory() != null) {
                paths.put("gameDirectory", profile.getGameDirectory().toString());
            }

            if (profile.getCustomJavaExecutable() != null) {
                paths.put("customJavaExecutable", profile.getCustomJavaExecutable().toString());
            }

            root.put("paths", paths);
        }

        // Launch options
        JsonObject launch = new JsonObject();
        launch.put("extraJvmArgs", writeStringList(profile.getExtraJvmArgs()));
        launch.put("extraGameArgs", writeStringList(profile.getExtraGameArgs()));
        launch.put("captureGameOutput", profile.isCaptureGameOutput());

        JsonObject environment = new JsonObject();
        profile.getEnvironmentVariables().forEach(environment::put);
        launch.put("environment", environment);

        if (profile.getServerAddress() != null) {
            launch.put("serverAddress", profile.getServerAddress());
        }

        if (profile.getLoader() != null) {
            launch.put("loader", serializeLoader(profile.getLoader()));
        }

        root.put("launch", launch);

        // User
        if (includeLocal) {
            JsonObject user = new JsonObject();
            user.put("offlineUsername", profile.getOfflineUsername());
            root.put("user", user);
        }

        // Files
        JsonArray filesJson = new JsonArray();
        for (ProfileFile file : files) {
            filesJson.add(serializeFile(file));
        }
        root.put("files", filesJson);

        return profileJson;
    }

    private static JsonObject serializeMetadata(ProfileMetadata metadata) {
        JsonObject json = new JsonObject();
        json.put("id", metadata.id());
        putIfNotNull(json, "name", metadata.name());
        putIfNotNull(json, "description", metadata.description());
        putIfNotNull(json, "author", metadata.author());
        putIfNotNull(json, "iconUrl", metadata.iconUrl());
        if (metadata.createdAt() != null) {
            json.put("createdAt", metadata.createdAt().toString());
        }
        if (metadata.updatedAt() != null) {
            json.put("updatedAt", metadata.updatedAt().toString());
        }
        return json;
    }

    private static ProfileMetadata readMetadata(JsonObject json) {
        String createdAt = json.getString("createdAt");
        String updatedAt = json.getString("updatedAt");
        return new ProfileMetadata(
                json.get("id").asString(),
                json.getString("name"),
                json.getString("description"),
                json.getString("author"),
                json.getString("iconUrl"),
                createdAt != null ? Instant.parse(createdAt) : null,
                updatedAt != null ? Instant.parse(updatedAt) : null
        );
    }

    private static JsonObject serializeFile(ProfileFile file) {
        JsonObject json = new JsonObject();
        json.put("path", file.path());
        putIfNotNull(json, "name", file.name());

        JsonObject hashes = new JsonObject();
        file.hashes().forEach(hashes::put);
        json.put("hashes", hashes);

        json.put("downloads", writeStringList(file.downloads()));

        if (file.size() >= 0) {
            json.put("fileSize", file.size());
        }

        JsonObject env = new JsonObject();
        env.put("client", file.client().id());
        env.put("server", file.server().id());
        json.put("env", env);

        json.put("enabled", file.enabled());

        if (file.source() != null) {
            JsonObject source = new JsonObject();
            source.put("platform", file.source().platform());
            putIfNotNull(source, "projectId", file.source().projectId());
            putIfNotNull(source, "versionId", file.source().versionId());
            json.put("source", source);
        }

        return json;
    }

    private static List<ProfileFile> readFiles(JsonArray array) {
        if (array == null) {
            return List.of();
        }

        List<ProfileFile> files = new ArrayList<>();
        for (JsonValue value : array) {
            JsonObject json = value.asObject();
            ProfileFile.Builder file = ProfileFile.builder(json.get("path").asString())
                    .name(json.getString("name"))
                    .downloads(readStringList(json.getArray("downloads")));

            JsonObject hashes = json.getObject("hashes");
            if (hashes != null) {
                for (String algorithm : hashes.keys()) {
                    file.hash(algorithm, hashes.getString(algorithm));
                }
            }

            Long size = json.getLong("fileSize");
            if (size != null) {
                file.size(size);
            }

            JsonObject env = json.getObject("env");
            if (env != null) {
                if (env.has("client")) {
                    file.client(SideRequirement.fromId(env.getString("client")));
                }
                if (env.has("server")) {
                    file.server(SideRequirement.fromId(env.getString("server")));
                }
            }

            Boolean enabled = json.getBoolean("enabled");
            if (enabled != null) {
                file.enabled(enabled);
            }

            JsonObject source = json.getObject("source");
            if (source != null) {
                file.source(new ProfileFileSource(
                        source.get("platform").asString(),
                        source.getString("projectId"),
                        source.getString("versionId")
                ));
            }

            files.add(file.build());
        }
        return files;
    }

    private static void applyEnvironment(ProfileEnvironment environment, LaunchConfiguration.Builder builder) {
        builder.jarFile(environment.jarFile())
                .librariesDirectory(environment.librariesDirectory())
                .assetsDirectory(environment.assetsDirectory());

        if (environment.nativesDirectory() != null) {
            builder.nativesDirectory(environment.nativesDirectory());
        }

        if (environment.customJavaExecutable() != null) {
            builder.customJavaExecutable(environment.customJavaExecutable());
        }

        if (environment.offlineUsername() != null) {
            builder.offlineUsername(environment.offlineUsername());
        }
    }

    private static void putIfNotNull(JsonObject json, String key, String value) {
        if (value != null) {
            json.put(key, value);
        }
    }

    private static JsonArray writeStringList(List<String> values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private static void applyPaths(JsonObject paths, LaunchConfiguration.Builder builder) {
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

        if (paths.has("gameDirectory")) {
            builder.gameDirectory(Path.of(paths.get("gameDirectory").asString()));
        }

        if (paths.has("customJavaExecutable")) {
            builder.customJavaExecutable(Path.of(paths.get("customJavaExecutable").asString()));
        }
    }

    private static JsonObject serializeLoader(Loader loader) {
        JsonObject json = new JsonObject();

        if (loader instanceof CustomLoader(
                String name,
                String mcVersion,
                String loaderVersion,
                String iconUrl,
                String mainClass,
                List<String> customJvmArgs,
                List<String> customGameArgs,
                List<String> customClasspathEntries
        )) {
            json.put("type", "CUSTOM");
            json.put("name", name);
            json.put("mcVersion", mcVersion);
            json.put("loaderVersion", loaderVersion);
            json.put("iconUrl", iconUrl);
            json.put("mainClass", mainClass);

            json.put("customJvmArgs", writeStringList(customJvmArgs));
            json.put("customGameArgs", writeStringList(customGameArgs));
            json.put("customClasspathEntries", writeStringList(customClasspathEntries));

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
            throw new UnexpectedLoaderException("Unknown loader type: " + type);
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
        if (array == null) {
            return List.of();
        }
        return array.values()
                .stream()
                .map(JsonValue::asString)
                .toList();
    }
}