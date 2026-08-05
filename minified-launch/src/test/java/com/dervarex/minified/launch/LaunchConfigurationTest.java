package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchConfigurationTest {
    @TempDir
    Path tempDir;

    @Test
    void builderAppliesCustomValues() {
        Path jarFile = tempDir.resolve("test.jar");
        Path assetsDir = tempDir.resolve("assets");
        Path libsDir = tempDir.resolve("libraries");
        Path nativesDir = tempDir.resolve("natives");

        LaunchConfiguration config = new LaunchConfiguration.Builder()
                .downloadThreads(12)
                .resolution(1280, 720)
                .launcherName("MinifiedLauncher")
                .launcherVersion("2.0.0")
                .isDemoUser(true)
                .extraJvmArgs(List.of("-Dtest=true", "-Dlauncher.name=minified"))
                .jarFile(jarFile)
                .assetsDirectory(assetsDir)
                .librariesDirectory(libsDir)
                .nativesDirectory(nativesDir)
                .loader(new VanillaLoader("1.21.11"))
                .build();

        assertEquals(12, config.getDownloadThreads());
        assertEquals(1280, config.getResolutionWidth());
        assertEquals(720, config.getResolutionHeight());
        assertTrue(config.isCustomResolution());
        assertEquals("MinifiedLauncher", config.getLauncherName());
        assertEquals("2.0.0", config.getLauncherVersion());
        assertTrue(config.isDemoUser());
        assertEquals(
                List.of("-Dtest=true", "-Dlauncher.name=minified"),
                config.getExtraJvmArgs()
        );
        assertEquals(jarFile.toAbsolutePath(), config.getJarFile().toAbsolutePath());
        assertEquals(assetsDir.toAbsolutePath(), config.getAssetsDirectory().toAbsolutePath());
        assertEquals(libsDir.toAbsolutePath(), config.getLibrariesDirectory().toAbsolutePath());
        assertEquals(nativesDir.toAbsolutePath(), config.getNativesDirectory().toAbsolutePath());
        assertEquals(new VanillaLoader("1.21.11"), config.getLoader());
    }
}