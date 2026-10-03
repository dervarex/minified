package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.launch.arguments.GameArgumentsParser;
import com.dervarex.minified.launch.arguments.JvmArgumentsParser;
import com.dervarex.minified.launch.arguments.LegacyMinecraftArgumentsParser;
import com.dervarex.minified.launch.exceptions.loader.UnexpectedLoaderException;
import com.dervarex.minified.launch.exceptions.version.MalformedVersionJsonException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.custom.CustomLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricProfileJsonLoader;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeProfileJsonLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoProfileJsonLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltProfileJsonLoader;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.launch.utils.X11Helper;
import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.json.*;
import org.apiguardian.api.API;

import java.util.List;

@API(status = API.Status.INTERNAL, consumers = {"com.dervarex.minified.launch.*"})
public class ArgumentsBuilder {
    public static List<String> buildJvmArguments(
            JsonFile versionJson,
            LaunchConfiguration launchConfig,
            com.dervarex.minified.launch.launch.internal.LaunchOptions options,
            Loader loader,
            String version,
            boolean online
    ) {
        JsonArray mergedJvm = new JsonArray();

        JsonValue argumentsValue = versionJson.get("arguments");
        if (argumentsValue != null) {
            JsonObject arguments = argumentsValue.asObject();

            JsonValue defaultUserJvmValue = arguments.get("default-user-jvm");
            if (defaultUserJvmValue != null) {
                for (JsonValue value : defaultUserJvmValue.asArray()) {
                    mergedJvm.add(value);
                }
            }

            JsonValue jvmValue = arguments.get("jvm");
            if (jvmValue != null) {
                for (JsonValue value : jvmValue.asArray()) {
                    mergedJvm.add(value);
                }
            }
        }

        List<String> jvmArgs = JvmArgumentsParser.parse(
                mergedJvm,
                launchConfig.getMinRam(),
                launchConfig.getMaxRam()
        );

        if (argumentsValue == null) {
            // pre 1.13 version JSONs (legacy "minecraftArguments") contain no jvm arguments, not even the classpath
            jvmArgs.add("-cp");
            jvmArgs.add("${classpath}");
        }

        jvmArgs.addAll(launchConfig.getExtraJvmArgs());
        jvmArgs.removeIf(arg -> arg.equals("-XX:+UseCompactObjectHeaders")); // I don't know if we should do it like that, but it seems to work fine
        jvmArgs.removeIf(arg ->
                arg.equals("--sun-misc-unsafe-memory-access=allow"));

        if (loader instanceof CustomLoader customLoader) {
            if (customLoader.customJvmArgs() != null) {
                jvmArgs.addAll(customLoader.customJvmArgs());
            }
        } else {
            JsonObject loaderProfileJson = loadLoaderProfileJson(loader, version, launchConfig, online);

            if (loaderProfileJson != null) {
                JsonValue fabricArguments = loaderProfileJson.get("arguments");
                if (fabricArguments != null && fabricArguments.asObject().get("jvm") != null) {
                    for (JsonValue e : fabricArguments.asObject().get("jvm").asArray()) {
                        jvmArgs.add(e.asString());
                    }
                }

                if (loader instanceof ForgeLoader && fabricArguments == null) {
                    // FML checks the signature of the vanilla jar, but Mojang re-signed the old jars
                    jvmArgs.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
                    jvmArgs.add("-Dfml.ignorePatchDiscrepancies=true");
                    // FML up to 1.5.2 downloads libraries from files.minecraftforge.net/fmllibs, which is gone
                    jvmArgs.add("-Dfml.core.libraries.mirror=" + ApiEndpoints.FML_LIBRARIES_MIRROR_URL);
                }
            }
        }

        return X11Helper.substituteVariables(jvmArgs, options.getVariables());
    }
    public static List<String> buildGameArguments(
            JsonFile versionJson,
            LaunchOptions options,
            Loader loader,
            String version,
            LaunchConfiguration launchConfig,
            boolean online
    ) {
        JsonValue argumentsValue = versionJson.get("arguments");

        if (argumentsValue == null) {
            // for pre 1.13 versions, a loader profile replaces the whole argument string (forge adds its --tweakClass there)
            JsonObject loaderProfileJson = loader instanceof CustomLoader
                    ? null
                    : loadLoaderProfileJson(loader, version, launchConfig, online);

            JsonValue minecraftArguments =
                    loaderProfileJson != null && loaderProfileJson.get("minecraftArguments") != null
                            ? loaderProfileJson.get("minecraftArguments")
                            : versionJson.get("minecraftArguments");
            if (minecraftArguments == null) {
                throw new MalformedVersionJsonException("No arguments or minecraftArguments found in version JSON");
            }

            return X11Helper.substituteVariables(
                    LegacyMinecraftArgumentsParser.parse(minecraftArguments.asString()),
                    options.getVariables()
            );
        }

        JsonObject arguments = argumentsValue.asObject();

        JsonValue gameValue = arguments.get("game");
        JsonArray gameArray = gameValue != null ? gameValue.asArray() : new JsonArray();

        if (loader instanceof CustomLoader customLoader) {
            if (customLoader.customGameArgs() != null) {
                for (String arg : customLoader.customGameArgs()) {
                    gameArray.add(JsonParser.parse(arg));
                }
            }
        } else {
            JsonObject loaderProfileJson = loadLoaderProfileJson(loader, version, launchConfig, online);

            if (loaderProfileJson != null) {
                JsonValue loaderArguments = loaderProfileJson.get("arguments");
                if (loaderArguments != null) {
                    JsonValue loaderGame = loaderArguments.asObject().get("game");
                    if (loaderGame != null) {
                        for (JsonValue arg : loaderGame.asArray()) {
                            gameArray.add(arg);
                        }
                    }
                }
            }
        }

        return GameArgumentsParser.parse(
                gameArray,
                options.getVariables(),
                options.getFeatures()
        );
    }

    /**
     * @return the version JSON of the mod loader, or null for vanilla
     */
    private static JsonObject loadLoaderProfileJson(
            Loader loader,
            String version,
            LaunchConfiguration launchConfig,
            boolean online
    ) {
        return switch (loader) {
            case VanillaLoader ignored -> null;
            case FabricLoader ignored -> FabricProfileJsonLoader.loadFabricProfileJson(version, launchConfig, online);
            case QuiltLoader ignored -> QuiltProfileJsonLoader.loadQuiltProfileJson(version, launchConfig, online);
            case ForgeLoader ignored -> ForgeProfileJsonLoader.loadForgeProfileJson(version, launchConfig, online);
            case NeoforgeLoader ignored -> NeoProfileJsonLoader.loadNeoforgeProfileJson(version, launchConfig, online);
            default -> throw new UnexpectedLoaderException("Unexpected loader: " + loader);
        };
    }
}
