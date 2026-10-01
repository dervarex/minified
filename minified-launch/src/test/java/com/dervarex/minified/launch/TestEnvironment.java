package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionFetcher;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionFetcher;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;

import java.nio.file.Path;
import java.util.Locale;

public final class TestEnvironment {

    private TestEnvironment() {
    }

    public static LaunchConfiguration config(Path tempDir) {
        String loaderName = System.getProperty("matrix.loader");
        String mcVersion  = System.getProperty("matrix.mcVersion");

        Loader loader = loaderName != null && mcVersion != null
                ? resolveLoader(loaderName, mcVersion)
                : new FabricLoader("1.21.11", "0.16.14");

        LaunchConfiguration.Builder builder = new LaunchConfiguration.Builder()
                .downloadThreads(10)
                .launcherName("MinifiedLauncher")
                .launcherVersion("1.0.0")
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("jar/libraries"))
                .jarFile(tempDir.resolve("jar/client.jar"))
                .isDemoUser(false)
                .loader(loader);

        if (loaderName != null && mcVersion != null) {
            builder.headless(Boolean.getBoolean("matrix.headless"));
            builder.headlessTimeoutSeconds(
                    Integer.getInteger("matrix.timeoutSeconds", 180));
            String marker = System.getProperty("matrix.marker");
            if (marker != null) {
                builder.headlessMarker(marker);
            }
        }

        return builder.build();
    }

    private static Loader resolveLoader(String name, String mcVersion) {
        String loaderVersion = System.getProperty("matrix.loaderVersion");
        try {
            return switch (name.toLowerCase(Locale.ROOT)) {
                case "vanilla" -> new VanillaLoader(mcVersion);
                case "fabric" -> new FabricLoader(mcVersion,
                        loaderVersion != null ? loaderVersion : FabricLoaderFetcher.getLatestLoaderVersion());
                case "quilt" -> new QuiltLoader(mcVersion,
                        loaderVersion != null ? loaderVersion : QuiltLoaderFetcher.getLatestLoaderVersion(mcVersion));
                case "forge" -> new ForgeLoader(mcVersion,
                        loaderVersion != null ? loaderVersion : new ForgeVersionFetcher().getLatest(mcVersion));
                case "neoforge" -> new NeoforgeLoader(mcVersion,
                        loaderVersion != null ? loaderVersion : new NeoVersionFetcher().getLatest(mcVersion));
                default -> throw new IllegalArgumentException("Unknown loader: " + name);
            };
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to resolve loader '" + name + "' for MC " + mcVersion, e);
        }
    }
}