package com.dervarex.minified.launch.launch;

import com.dervarex.minified.launch.arguments.GameArgumentsParser;
import com.dervarex.minified.launch.arguments.JvmArgumentsParser;
import com.dervarex.minified.launch.arguments.LegacyMinecraftArgumentsParser;
import com.dervarex.minified.launch.exceptions.loader.UnexpectedLoaderException;
import com.dervarex.minified.launch.exceptions.version.MalformedVersionJsonException;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.custom.CustomLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.launch.utils.X11Helper;
import com.dervarex.minified.utils.json.*;

import java.util.List;

public class ArgumentsBuilder {
    static List<String> buildJvmArguments(
            JsonFile versionJson,
            LaunchConfiguration launchConfig,
            LaunchOptions options,
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

        jvmArgs.addAll(launchConfig.getExtraJvmArgs());
        jvmArgs.removeIf(arg -> arg.equals("-XX:+UseCompactObjectHeaders")); // I don't know if we should do it like that, but it seems to work fine
        jvmArgs.removeIf(arg ->
                arg.equals("--sun-misc-unsafe-memory-access=allow"));

        if (loader instanceof CustomLoader customLoader) {
            if (customLoader.customJvmArgs() != null) {
                jvmArgs.addAll(customLoader.customJvmArgs());
            }
        } else {
            JsonObject loaderProfileJson = null;

            switch (loader) {
                case VanillaLoader ignored:
                    break;
                case FabricLoader ignored:
                    loaderProfileJson = FabricLoaderFetcher.loadFabricProfileJson(version, online);
                    break;
                case ForgeLoader ignored:
                    loaderProfileJson = ForgeLoaderFetcher.loadForgeProfileJson(version, launchConfig, online);
                    break;
                case NeoforgeLoader ignored:
                    loaderProfileJson = NeoLoaderFetcher.loadNeoForgeProfileJson(version, launchConfig, online);
                    break;
                case QuiltLoader ignored:
                    loaderProfileJson = QuiltLoaderFetcher.loadQuiltProfileJson(version, online);
                    break;
                default:
                    throw new UnexpectedLoaderException("Unexpected loader: " + loader);
            }

            if (loaderProfileJson != null) {
                JsonValue fabricArguments = loaderProfileJson.get("arguments");
                if (fabricArguments != null && fabricArguments.asObject().get("jvm") != null) {
                    for (JsonValue e : fabricArguments.asObject().get("jvm").asArray()) {
                        jvmArgs.add(e.asString());
                    }
                }
            }
        }

        return X11Helper.substituteVariables(jvmArgs, options.getVariables());
    }
    static List<String> buildGameArguments(
            JsonFile versionJson,
            LaunchOptions options,
            Loader loader,
            String version,
            LaunchConfiguration launchConfig,
            boolean online
    ) {
        JsonValue argumentsValue = versionJson.get("arguments");

        if (argumentsValue == null) {
            JsonValue minecraftArguments = versionJson.get("minecraftArguments");
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
            JsonObject loaderProfileJson = null;

            switch (loader) {
                case VanillaLoader ignored:
                    break;
                case FabricLoader ignored:
                    loaderProfileJson = FabricLoaderFetcher.loadFabricProfileJson(version, online);
                    break;
                case QuiltLoader ignored:
                    loaderProfileJson = QuiltLoaderFetcher.loadQuiltProfileJson(version, online);
                    break;
                case ForgeLoader ignored:
                    loaderProfileJson = ForgeLoaderFetcher.loadForgeProfileJson(version, launchConfig, online);
                    break;
                case NeoforgeLoader ignored:
                    loaderProfileJson = NeoLoaderFetcher.loadNeoForgeProfileJson(version, launchConfig, online);
                    break;
                default:
                    throw new UnexpectedLoaderException("Unexpected loader: " + loader);
            }

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
}
