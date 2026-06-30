package com.dervarex.minified.java;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;

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

