package com.dervarex.minified.launch.profile;

import com.dervarex.minified.launch.exceptions.loader.UnknownLoaderTypeException;
import com.dervarex.minified.launch.exceptions.profile.FailedToLoadProfileException;
import com.dervarex.minified.launch.exceptions.profile.FailedToSaveProfileException;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.Launcher;
import com.dervarex.minified.launch.launch.modding.Loader;
import com.dervarex.minified.launch.launch.modding.custom.CustomLoader;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoader;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.neoforge.NeoforgeLoader;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoader;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileFactoryTest {
    @TempDir
    Path tempDir;

    @Test
    void canSaveAndLoadConfiguration() {
        LaunchConfiguration[] pair = createConfigurationPair();

        LaunchConfiguration baseConfig = pair[0];
        LaunchConfiguration config = pair[1];

        assertEquals(baseConfig.getMinRam(), config.getMinRam());
        assertEquals(baseConfig.getMaxRam(), config.getMaxRam());
        assertEquals(baseConfig.getDownloadThreads(), config.getDownloadThreads());
        assertEquals(baseConfig.getResolutionWidth(), config.getResolutionWidth());
        assertEquals(baseConfig.getResolutionHeight(), config.getResolutionHeight());
        assertEquals(baseConfig.isCustomResolution(), config.isCustomResolution());
        assertEquals(baseConfig.getLauncherName(), config.getLauncherName());
        assertEquals(baseConfig.getLauncherVersion(), config.getLauncherVersion());
        assertEquals(baseConfig.isDemoUser(), config.isDemoUser());
        assertEquals(baseConfig.getExtraJvmArgs(), config.getExtraJvmArgs());
        assertEquals(baseConfig.getJarFile(), config.getJarFile());
        assertEquals(baseConfig.getAssetsDirectory(), config.getAssetsDirectory());
        assertEquals(baseConfig.getLibrariesDirectory(), config.getLibrariesDirectory());
        assertEquals(baseConfig.getNativesDirectory(), config.getNativesDirectory());
        assertEquals(baseConfig.getCustomJavaExecutable(), config.getCustomJavaExecutable());
        assertEquals(baseConfig.getOfflineUsername(), config.getOfflineUsername());

    /*
      Don't be confused: assertEquals() uses .equals(), while assertNotSame() checks reference equality.
      We want the loader to be a different instance while still being equal.
     */
        assertEquals(baseConfig.getLoader(), config.getLoader());
        assertNotSame(baseConfig.getLoader(), config.getLoader());
    }

    @ParameterizedTest
    @MethodSource("loaders")
    void keepsEveryLoaderType(Loader loader) {
        Path profile = tempDir.resolve("profile.json");
        ProfileFactory.save(minimalConfiguration(loader), profile);

        assertEquals(loader, ProfileFactory.load(profile).getLoader());
    }

    @Test
    void unknownLoaderTypesAreNotWrapped() throws IOException {
        Path profile = tempDir.resolve("profile.json");
        ProfileFactory.save(minimalConfiguration(new VanillaLoader("1.21.11")), profile);
        Files.writeString(profile, Files.readString(profile).replace("\"VANILLA\"", "\"LITELOADER\""));

        assertThrows(UnknownLoaderTypeException.class, () -> ProfileFactory.load(profile));
    }

    @Test
    void brokenProfilesFailToLoad() throws IOException {
        Path profile = Files.writeString(tempDir.resolve("profile.json"), "{}");

        assertThrows(FailedToLoadProfileException.class, () -> ProfileFactory.load(profile));
        assertThrows(FailedToLoadProfileException.class, () -> ProfileFactory.load(tempDir.resolve("does-not-exist.json")));
    }

    @Test
    void failsToSaveIntoADirectory() {
        assertThrows(FailedToSaveProfileException.class,
                () -> ProfileFactory.save(minimalConfiguration(new VanillaLoader("1.21.11")), tempDir));
    }

    @Tag("manual")
    @Test
    void canLaunchLoadedConfiguration() {
        assertDoesNotThrow(() ->
                Launcher.launchMinecraft(
                        null,
                        createConfigurationPair()[1]
                )
        );
    }

    static List<Loader> loaders() {
        return List.of(
                new FabricLoader("1.21.11", "0.16.14"),
                new QuiltLoader("1.21.11", "0.29.2"),
                new ForgeLoader("1.21.11", "1.21.11-61.1.8"),
                new NeoforgeLoader("1.21.11", "21.11.20"),
                new CustomLoader("Homebrew", "1.21.11", "0.0.1", "https://example.com/icon.png", "net.example.Main",
                        List.of("-Dhomebrew=true"), List.of("--brew"), List.of("/opt/homebrew.jar"))
        );
    }

    private LaunchConfiguration minimalConfiguration(Loader loader) {
        return new LaunchConfiguration.Builder()
                .jarFile(tempDir.resolve("client.jar"))
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("libraries"))
                .loader(loader)
                .build();
    }

    private LaunchConfiguration createLaunchConfiguration() {
        return new LaunchConfiguration.Builder()
                .minRam(1024)
                .maxRam(6144)
                .downloadThreads(12)
                .resolution(1280, 720)
                .launcherName("MinifiedLauncher")
                .launcherVersion("2.0.0")
                .isDemoUser(true)
                //.extraJvmArg("-XX:+UseG1GC")
                .extraJvmArgs(List.of("-Dtest=true", "-Dlauncher.name=minified"))
                .jarFile(tempDir.resolve("test.jar"))
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("libraries"))
                .nativesDirectory(tempDir.resolve("natives"))
                .customJavaExecutable(tempDir.resolve("java/bin/java"))
                .offlineUsername("Notch")
                .loader(new VanillaLoader("26.1.2"))
                .isDemoUser(false)
                .build();
    }

    private LaunchConfiguration[] createConfigurationPair() {
        LaunchConfiguration original = createLaunchConfiguration();
        Path profile = tempDir.resolve("profile.json");
        ProfileFactory.save(original, profile);
        LaunchConfiguration loaded = ProfileFactory.load(profile);
        return new LaunchConfiguration[]{original, loaded};
    }

}
