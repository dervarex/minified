package com.dervarex.minified.launch.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class X11Helper {
    /**
     * Normalizes the child JVM graphics environment so Linux launches can
     * fall back to X11 when a local Xwayland display is available.
     * @param processBuilder the process builder for the child JVM,<p> which will be modified in-place to set DISPLAY and potentially XDG_SESSION_TYPE
     */
    public static void configureGraphicsEnvironment(
            ProcessBuilder processBuilder
    ) {
        if (!System.getProperty("os.name")
                .toLowerCase()
                .contains("linux")) {
            return;
        }
        configureGraphicsEnvironment(
                processBuilder,
                Path.of("/tmp/.X11-unix")
        );
    }

    /**
     * Normalizes the child JVM graphics environment using the supplied X11
     * socket directory.
     */
    public static void configureGraphicsEnvironment(
            ProcessBuilder processBuilder,
            Path x11SocketDirectory
    ) {
        if (!System.getProperty("os.name")
                .toLowerCase()
                .contains("linux")) {
            return;
        }
        Map<String, String> environment =
                processBuilder.environment();

        String display = environment.get("DISPLAY");

        if (display != null && !display.isBlank()) {
            return;
        }

        String resolvedDisplay = resolveDisplay(x11SocketDirectory);

        if (resolvedDisplay == null) {
            return;
        }

        environment.put("DISPLAY", resolvedDisplay);

        String waylandDisplay = environment.get("WAYLAND_DISPLAY");

        if (waylandDisplay != null && !waylandDisplay.isBlank()) {
            environment.remove("WAYLAND_DISPLAY");
            environment.put("XDG_SESSION_TYPE", "x11");
        }

        System.out.println(
                "Detected X11 display " + resolvedDisplay +
                        " for Minecraft"
        );
    }

    /**
     * Attempts to infer an active X11 display from socket names such as X0,
     * X1, and so on.
     * @param x11SocketDirectory the directory containing X11 socket files, typically /tmp/.X11-unix
     * @return a display string such as :0, :1, etc., or null if no valid display could be inferred
     */
    public static String resolveDisplay(
            Path x11SocketDirectory
    ) {
        if (x11SocketDirectory == null || !Files.isDirectory(x11SocketDirectory)) {
            return null;
        }

        try (var sockets = Files.list(x11SocketDirectory)) {
            return sockets
                    .map(path -> parseDisplayNumber(
                            path.getFileName().toString()
                    ))
                    .filter(Objects::nonNull)
                    .sorted()
                    .findFirst()
                    .map(number -> ":" + number)
                    .orElse(null);
        } catch (IOException ignored) {
            return null;
        }
    }

    public static Integer parseDisplayNumber(
            String socketName
    ) {
        if (!socketName.startsWith("X")) {
            return null;
        }

        try {
            return Integer.parseInt(socketName.substring(1));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Replaces ${key} placeholders in the supplied arguments.
     */
    public static List<String> substituteVariables(
            List<String> arguments,
            Map<String, String> variables
    ) {
        ArrayList<String> result = new ArrayList<>();
        for (String arg : arguments) {
            String substituted = arg;
            for (String key : variables.keySet()) {
                String value = variables.get(key);
                if (value == null) {
                    value = "";
                }
                substituted = substituted.replace("${" + key + "}", value);
            }
            result.add(substituted);
        }
        return result;
    }
}
