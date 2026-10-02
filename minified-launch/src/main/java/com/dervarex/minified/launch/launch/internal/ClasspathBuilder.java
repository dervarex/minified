package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.launch.exceptions.libraries.FailedToLoadLibrariesException;
import com.dervarex.minified.launch.exceptions.loader.UnexpectedLoaderException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricProfileJsonLoader;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionJson;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionJson;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltProfileJsonLoader;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonValue;
import com.dervarex.minified.utils.os.OS;
import org.apiguardian.api.API;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
public class ClasspathBuilder {
    public static String buildClasspath(JsonFile versionJson, LaunchConfiguration config, boolean online) {
        String separator = System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ":";

        // exact duplicates (e.g. commons-lang3 in both the loader and the vanilla profile) crash NeoForge :(
        Set<String> classpath = new LinkedHashSet<>();
        Set<String> loaderArtifacts = new HashSet<>();

        boolean includeClientJar = true;

        // like the official launcher we do loader libs before vanilla libs
        // Loaders ship newer versions of some vanilla libraries (forge 1.16.5: log4j 2.15.0 instead of 2.8.1),
        // an older one would be missing stuff
        switch (config.getLoader()) {
            case VanillaLoader ignored:
                break;

            case FabricLoader ignored:
                try {
                    JsonObject fabricProfile =
                            FabricProfileJsonLoader.loadFabricProfileJson(
                                    versionJson.get("id").asString(),
                                    config,
                                    online
                            );

                    addModLoaderLibraries(
                            fabricProfile.get("libraries").asArray(),
                            classpath,
                            loaderArtifacts,
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
                            QuiltProfileJsonLoader.loadQuiltProfileJson(
                                    versionJson.get("id").asString(),
                                    config,
                                    online
                            );

                    addModLoaderLibraries(
                            quiltProfile.get("libraries").asArray(),
                            classpath,
                            loaderArtifacts,
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
            case NeoforgeLoader neoforgeLoader:
                try {
                    JsonObject neoForgeProfile =
                            NeoVersionJson.getVersionJson(
                                    config.getJarFile().getParent(),
                                    neoforgeLoader.loaderVersion()
                            ).asObject();

                    addModLoaderLibraries(
                            neoForgeProfile.get("libraries").asArray(),
                            classpath,
                            loaderArtifacts,
                            config
                    );

                    includeClientJar = !ignoresVanillaJar(neoForgeProfile);
                } catch (Exception e) {
                    throw new FailedToLoadLibrariesException(
                            "Failed to load NeoForge libraries",
                            config.getLoader(),
                            e
                    );
                }
                break;
            case ForgeLoader forgeLoader:
                try {
                    JsonObject forgeProfile =
                            ForgeVersionJson.getVersionJson(
                                    config.getJarFile().getParent(),
                                    forgeLoader.loaderVersion()
                            ).asObject();

                    addModLoaderLibraries(
                            forgeProfile.get("libraries").asArray(),
                            classpath,
                            loaderArtifacts,
                            config
                    );

                    includeClientJar = !ignoresVanillaJar(forgeProfile);
                } catch (Exception e) {
                    throw new FailedToLoadLibrariesException(
                            "Failed to load Forge libraries",
                            config.getLoader(),
                            e
                    );
                }
                break;
            default:
                throw new UnexpectedLoaderException("Unexpected loader: " + config.getLoader());
        }

        JsonArray libraries = versionJson.get("libraries").asArray();
        for (JsonValue value : libraries) {
            JsonObject library = value.asObject();
            // the loader's version of a library replaces the vanilla one (neoforge 21.4: asm 9.8 instead of 9.6)
            if (loaderArtifacts.contains(artifactKey(library))) {
                continue;
            }
            addLibrary(library, classpath, config);
        }

        // like the official launcher, the game jar comes after the libs, loaders that bring their own patched
        // mc jar as a library (forge 1.21+) use the first one they find, which has to be theirs
        if (includeClientJar) {
            classpath.add(
                    config.getJarFile()
                            .toAbsolutePath()
                            .toString()
            );
        }

        return String.join(
                separator,
                classpath
        );
    }

    /**
     * BootstrapLauncher (forge 1.17+, neoforge) turns every classpath entry into a module, unless its file name starts
     * with {@code -DignoreList}. The loader profiles exclude the vanilla jar using {@code ${version_name}.jar},
     * which does not match our jar file name <br>
     * If we don't do this, it nukes itself
     *
     * @param loaderProfile the version JSON of the mod loader
     * @return true if the loader does not want the vanilla jar on the classpath
     */
    private static boolean ignoresVanillaJar(JsonObject loaderProfile) {
        JsonValue argumentsValue = loaderProfile.get("arguments");
        if (argumentsValue == null || argumentsValue.asObject().get("jvm") == null) {
            return false;
        }

        for (JsonValue argument : argumentsValue.asObject().get("jvm").asArray()) {
            if (argument.isString()
                    && argument.asString().startsWith("-DignoreList=")
                    && argument.asString().contains("${version_name}.jar")) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param library a library of a version JSON
     * @return group, artifact and classifier without the version, e.g. {@code org.ow2.asm:asm} for
     * {@code org.ow2.asm:asm:9.6}, or null if the library has no name
     */
    private static String artifactKey(JsonObject library) {
        JsonValue nameValue = library.get("name");
        if (nameValue == null) {
            return null;
        }

        String[] parts = nameValue.asString().split("@")[0].split(":");
        if (parts.length < 3) {
            return null;
        }

        return parts.length > 3
                ? parts[0] + ":" + parts[1] + ":" + parts[3]
                : parts[0] + ":" + parts[1];
    }

    private static void addLibrary(
            JsonObject library,
            Set<String> classpath,
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
            Set<String> classpath,
            Set<String> loaderArtifacts,
            LaunchConfiguration config
    ) {
        for (JsonValue value : libraries) {
            JsonObject library = value.asObject();
            String artifactKey = artifactKey(library);
            if (artifactKey != null && isAllowed(library)) {
                loaderArtifacts.add(artifactKey);
            }
            addLibrary(
                    library,
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
        String os = OS.getCurrentOS().getName();

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