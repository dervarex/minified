package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.Launcher;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

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
            Launcher.launchMinecraft(null, TestEnvironment.config(tempDir));
            status = "pass";
            message = "Reached main menu";
        } catch (Throwable t) {
            status = "fail";
            message = t.getClass().getSimpleName()
                    + (t.getMessage() != null ? ": " + t.getMessage() : "");
        }

        writeResult(resultFile, loader, mcVersion, status, message);

        assertEquals("pass", status, message);
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
                message.replace("\\", "\\\\").replace("\"", "\\\""),
                Instant.now().toString()
        );
        Files.writeString(file, json);
    }
}