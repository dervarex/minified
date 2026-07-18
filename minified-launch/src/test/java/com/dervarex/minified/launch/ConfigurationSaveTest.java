package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.Launcher;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.launch.profile.ProfileFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class ConfigurationSaveTest {
    @TempDir
    Path tempDir;

    @Test
    void canSaveAndLoadConfiguration() {
        LaunchConfiguration[] pair = createConfigurationPair();

        LaunchConfiguration baseConfig = pair[0];
        LaunchConfiguration config = pair[1];

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

    /*
      Don't be confused: assertEquals() uses .equals(), while assertNotSame() checks reference equality.
      We want the loader to be a different instance while still being equal.
     */
        assertEquals(baseConfig.getLoader(), config.getLoader());
        assertNotSame(baseConfig.getLoader(), config.getLoader());
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

    private LaunchConfiguration createLaunchConfiguration() {
        return new LaunchConfiguration.Builder()
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
