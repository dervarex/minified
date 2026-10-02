package com.dervarex.minified.launch;

import com.dervarex.minified.launch.events.launch.GameStoppedEvent;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.Launcher;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("manual")
@Tag("version-matrix")
class VersionSupportTest {

    @TempDir
    Path tempDir;

    @Test
    void launchHeadless() throws IOException {
        String loader = System.getProperty("matrix.loader", "vanilla");
        String mcVersion = System.getProperty("matrix.mcVersion", "1.21.11");

        Path resultFile = resolveResultFile(loader, mcVersion);

        String status;
        String message;
        try {
            LaunchConfiguration config = TestEnvironment.config(tempDir);
            AtomicInteger exitCode = new AtomicInteger(Integer.MIN_VALUE);
            config.getEventBus().subscribe(GameStoppedEvent.class, event -> exitCode.set(event.exitCode()));

            Launcher.launchMinecraft(null, config);

            if (exitCode.get() == 0) {
                status = "pass";
                message = "Reached main menu";
            } else {
                status = "fail";
                message = "Game did not reach the main menu (exit code " + exitCode.get() + ")";
            }
        } catch (Throwable t) {
            t.printStackTrace();
            status = "fail";
            message = describe(t);
            Throwable root = t;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            if (root != t) {
                message += " (caused by " + describe(root) + ")";
            }
        }

        writeResult(resultFile, loader, mcVersion, status, message);

        assertEquals("pass", status, message);
    }

    private static String describe(Throwable t) {
        return t.getClass().getSimpleName()
                + (t.getMessage() != null ? ": " + t.getMessage() : "");
    }

    private Path resolveResultFile(String loader, String mcVersion) throws IOException {
        String explicit = System.getProperty("matrix.resultFile");
        Path file = explicit != null
                ? Path.of(explicit)
                : Path.of("build", "matrix-results", loader + "-" + mcVersion + ".json");
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        return file;
    }

    private void writeResult(
            Path file,
            String loader,
            String mcVersion,
            String status,
            String message
    ) throws IOException {
        String json = """
                {
                  "loader": "%s",
                  "mcVersion": "%s",
                  "status": "%s",
                  "message": "%s",
                  "timestamp": "%s"
                }
                """.formatted(
                loader,
                mcVersion,
                status,
                message.replace("\\", "\\\\").replace("\"", "\\\"")
                        .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t"),
                Instant.now().toString()
        );
        Files.writeString(file, json);
    }
}