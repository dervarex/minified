package com.dervarex.minified.java;

import com.dervarex.minified.utils.json.JsonFile;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaManagerTest {

    @Test
    void currentRuntimePointsToAnExistingExecutable() {
        JavaInstallation installation = JavaManager.currentRuntime();

        assertEquals(JavaPlatform.majorVersion(), installation.majorVersion());
        assertTrue(Files.exists(installation.executable()));
    }
}

