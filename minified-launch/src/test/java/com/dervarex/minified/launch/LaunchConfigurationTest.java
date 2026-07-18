package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LaunchConfigurationTest {

    @Test
    void builderAppliesCustomValues() {
        LaunchConfiguration config = new LaunchConfiguration.Builder()
                .downloadThreads(12)
                .resolution(1280, 720)
                .launcherName("MinifiedLauncher")
                .launcherVersion("2.0.0")
                .isDemoUser(true)
                .extraJvmArgs(List.of("-Dtest=true", "-Dlauncher.name=minified"))
                .build();

        assertEquals(12, config.getDownloadThreads());
        assertEquals(1280, config.getResolutionWidth());
        assertEquals(720, config.getResolutionHeight());
        assertTrue(config.isCustomResolution());
        assertEquals("MinifiedLauncher", config.getLauncherName());
        assertEquals("2.0.0", config.getLauncherVersion());
        assertTrue(config.isDemoUser());
        assertEquals(
                List.of("-XX:+UseG1GC", "-Dtest=true", "-Dlauncher.name=minified"),
                config.getExtraJvmArgs()
        );
    }
}
