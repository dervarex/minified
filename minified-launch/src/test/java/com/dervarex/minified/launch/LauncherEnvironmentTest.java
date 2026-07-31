package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.LaunchContext;
import com.dervarex.minified.launch.utils.X11Helper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LauncherEnvironmentTest {

    @TempDir
    Path tempDir;

    @Test
    void resolveDisplayPrefersLowestAvailableX11Socket() throws Exception {
        Files.createFile(tempDir.resolve("X12"));
        Files.createFile(tempDir.resolve("X2"));
        Files.createFile(tempDir.resolve("X7"));

        assertEquals(":2", X11Helper.resolveDisplay(tempDir));
    }

    @Test
    void resolveDisplayIgnoresNonSocketEntries() throws Exception {
        Files.createFile(tempDir.resolve("README"));
        Files.createFile(tempDir.resolve("Xbroken"));
        Files.createFile(tempDir.resolve("X3"));

        assertEquals(":3", X11Helper.resolveDisplay(tempDir));
    }

    @Test
    void resolveDisplayReturnsNullWhenDirectoryIsMissingOrEmpty() {
        assertNull(X11Helper.resolveDisplay(tempDir.resolve("does-not-exist")));
        assertNull(X11Helper.resolveDisplay(tempDir));
    }

    @Test
    void configureGraphicsEnvironmentDoesNotOverrideExistingDisplay() throws Exception {
        Files.createFile(tempDir.resolve("X1"));

        ProcessBuilder processBuilder = new ProcessBuilder("java");
        processBuilder.environment().put("DISPLAY", ":9");
        processBuilder.environment().put("WAYLAND_DISPLAY", "wayland-1");
        processBuilder.environment().remove("XDG_SESSION_TYPE");

        X11Helper.configureGraphicsEnvironment(processBuilder, new LaunchContext(null, TestEnvironment.config(tempDir)));

        assertEquals(":9", processBuilder.environment().get("DISPLAY"));
        assertEquals("wayland-1", processBuilder.environment().get("WAYLAND_DISPLAY"));
        assertNull(processBuilder.environment().get("XDG_SESSION_TYPE"));
    }

    @Test
    void configureGraphicsEnvironmentPromotesWaylandSessionToX11WhenSocketExists() throws Exception {
        Files.createFile(tempDir.resolve("X5"));

        ProcessBuilder processBuilder = new ProcessBuilder("java");
        processBuilder.environment().remove("DISPLAY");
        processBuilder.environment().put("WAYLAND_DISPLAY", "wayland-1");
        processBuilder.environment().remove("XDG_SESSION_TYPE");

        X11Helper.configureGraphicsEnvironment(processBuilder, new LaunchContext(null, TestEnvironment.config(tempDir)));

        assertEquals(":5", processBuilder.environment().get("DISPLAY"));
        assertNull(processBuilder.environment().get("WAYLAND_DISPLAY"));
        assertEquals("x11", processBuilder.environment().get("XDG_SESSION_TYPE"));
    }

    @Test
    void configureGraphicsEnvironmentLeavesWaylandAloneWhenNoSocketExists() {
        ProcessBuilder processBuilder = new ProcessBuilder("java");
        processBuilder.environment().remove("DISPLAY");
        processBuilder.environment().put("WAYLAND_DISPLAY", "wayland-1");
        processBuilder.environment().remove("XDG_SESSION_TYPE");

        X11Helper.configureGraphicsEnvironment(processBuilder, new LaunchContext(null, TestEnvironment.config(tempDir)));

        assertNull(processBuilder.environment().get("DISPLAY"));
        assertEquals("wayland-1", processBuilder.environment().get("WAYLAND_DISPLAY"));
        assertNull(processBuilder.environment().get("XDG_SESSION_TYPE"));
    }
}
