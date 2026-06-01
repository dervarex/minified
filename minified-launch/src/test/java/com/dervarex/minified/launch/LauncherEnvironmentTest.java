package com.dervarex.minified.launch;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LauncherEnvironmentTest {

    @Test
    void resolveDisplayPrefersLowestAvailableX11Socket() throws Exception {
        Path socketDirectory = Files.createTempDirectory("x11-sockets");

        Files.createFile(socketDirectory.resolve("X12"));
        Files.createFile(socketDirectory.resolve("X2"));
        Files.createFile(socketDirectory.resolve("X7"));

        assertEquals(
                ":2",
                Launcher.resolveDisplay(socketDirectory)
        );
    }

    @Test
    void configureGraphicsEnvironmentDoesNotOverrideExistingDisplay() throws Exception {
        Path socketDirectory = Files.createTempDirectory("x11-sockets");
        Files.createFile(socketDirectory.resolve("X1"));

        ProcessBuilder processBuilder = new ProcessBuilder("java");
        processBuilder.environment().put("DISPLAY", ":9");
        processBuilder.environment().put("WAYLAND_DISPLAY", "wayland-1");

        Launcher.configureGraphicsEnvironment(
                processBuilder,
                socketDirectory
        );

        assertEquals(
                ":9",
                processBuilder.environment().get("DISPLAY")
        );
        assertEquals(
                "wayland-1",
                processBuilder.environment().get("WAYLAND_DISPLAY")
        );
    }

    @Test
    void configureGraphicsEnvironmentPromotesWaylandSessionToX11WhenSocketExists() throws Exception {
        Path socketDirectory = Files.createTempDirectory("x11-sockets");
        Files.createFile(socketDirectory.resolve("X5"));

        ProcessBuilder processBuilder = new ProcessBuilder("java");
        processBuilder.environment().remove("DISPLAY");
        processBuilder.environment().put("WAYLAND_DISPLAY", "wayland-1");

        Launcher.configureGraphicsEnvironment(
                processBuilder,
                socketDirectory
        );

        assertEquals(
                ":5",
                processBuilder.environment().get("DISPLAY")
        );
        assertNull(processBuilder.environment().get("WAYLAND_DISPLAY"));
        assertEquals(
                "x11",
                processBuilder.environment().get("XDG_SESSION_TYPE")
        );
    }
}

