package com.dervarex.minified.launch;

import com.dervarex.minified.launch.launch.Launcher;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

@Tag("manual")
public class LaunchTest {
    @TempDir
    public Path tempDir;

    @Test
     void launch() {
        Launcher.launchMinecraft(
                null,
                TestEnvironment.config(tempDir)
        );
    }
}