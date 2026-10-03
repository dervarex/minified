package com.dervarex.minified.launch.utils;

import com.dervarex.minified.launch.TestEnvironment;
import com.dervarex.minified.launch.events.environment.ConfigureX11EnvironmentEvent;
import com.dervarex.minified.launch.launch.LaunchContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class X11HelperTest {

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
    void substituteVariablesReplacesEveryPlaceholder() {
        Map<String, String> variables = new HashMap<>();
        variables.put("natives_directory", "/natives");
        variables.put("launcher_name", null);

        assertEquals(
                List.of("-Djava.library.path=/natives", "-Dminecraft.launcher.brand=", "${unknown}"),
                X11Helper.substituteVariables(
                        List.of("-Djava.library.path=${natives_directory}", "-Dminecraft.launcher.brand=${launcher_name}", "${unknown}"),
                        variables
                )
        );
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void configureGraphicsEnvironmentDoesNotOverrideExistingDisplay() throws Exception {
        Files.createFile(tempDir.resolve("X1"));

        ProcessBuilder processBuilder = new ProcessBuilder("java");
        processBuilder.environment().put("DISPLAY", ":9");
        processBuilder.environment().put("WAYLAND_DISPLAY", "wayland-1");
        processBuilder.environment().remove("XDG_SESSION_TYPE");

        X11Helper.configureGraphicsEnvironment(processBuilder, tempDir, context());

        assertEquals(":9", processBuilder.environment().get("DISPLAY"));
        assertEquals("wayland-1", processBuilder.environment().get("WAYLAND_DISPLAY"));
        assertNull(processBuilder.environment().get("XDG_SESSION_TYPE"));
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void configureGraphicsEnvironmentPromotesWaylandSessionToX11WhenSocketExists() throws Exception {
        Files.createFile(tempDir.resolve("X5"));

        ProcessBuilder processBuilder = new ProcessBuilder("java");
        processBuilder.environment().remove("DISPLAY");
        processBuilder.environment().put("WAYLAND_DISPLAY", "wayland-1");
        processBuilder.environment().remove("XDG_SESSION_TYPE");

        LaunchContext context = context();
        AtomicReference<String> posted = new AtomicReference<>();
        context.getEventBus().subscribe(ConfigureX11EnvironmentEvent.class, event -> posted.set(event.display()));

        X11Helper.configureGraphicsEnvironment(processBuilder, tempDir, context);

        assertEquals(":5", processBuilder.environment().get("DISPLAY"));
        assertNull(processBuilder.environment().get("WAYLAND_DISPLAY"));
        assertEquals("x11", processBuilder.environment().get("XDG_SESSION_TYPE"));
        assertEquals(":5", posted.get());
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void configureGraphicsEnvironmentLeavesWaylandAloneWhenNoSocketExists() {
        ProcessBuilder processBuilder = new ProcessBuilder("java");
        processBuilder.environment().remove("DISPLAY");
        processBuilder.environment().put("WAYLAND_DISPLAY", "wayland-1");
        processBuilder.environment().remove("XDG_SESSION_TYPE");

        X11Helper.configureGraphicsEnvironment(processBuilder, tempDir, context());

        assertNull(processBuilder.environment().get("DISPLAY"));
        assertEquals("wayland-1", processBuilder.environment().get("WAYLAND_DISPLAY"));
        assertNull(processBuilder.environment().get("XDG_SESSION_TYPE"));
    }

    private LaunchContext context() {
        return new LaunchContext(null, TestEnvironment.config(tempDir));
    }
}
