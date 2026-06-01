package com.dervarex.minified.launch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchConfiguratorTest {

    @Test
    void builderUsesSaneDefaults() {
        LaunchConfigurator config = new LaunchConfigurator.Builder().build();

        assertEquals(5, config.getDownloadThreads());
        assertEquals(1920, config.getResolutionWidth());
        assertEquals(1080, config.getResolutionHeight());
        assertFalse(config.isCustomResolution());
        assertEquals("Launcher", config.getLauncherName());
        assertEquals("1.0.0", config.getLauncherVersion());
        assertFalse(config.isDemoUser());
    }

    @Test
    void builderAppliesCustomValues() {
        LaunchConfigurator config = new LaunchConfigurator.Builder()
                .downloadThreads(12)
                .resolution(1280, 720)
                .launcherName("MinifiedLauncher")
                .launcherVersion("2.0.1")
                .isDemoUser(true)
                .build();

        assertEquals(12, config.getDownloadThreads());
        assertEquals(1280, config.getResolutionWidth());
        assertEquals(720, config.getResolutionHeight());
        assertTrue(config.isCustomResolution());
        assertEquals("MinifiedLauncher", config.getLauncherName());
        assertEquals("2.0.1", config.getLauncherVersion());
        assertTrue(config.isDemoUser());
    }
}

