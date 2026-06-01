package com.dervarex.minified.java;

import com.dervarex.minified.utils.json.JsonFile;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaManagerTest {
    @Test
    void resolvesRequiredJavaVersionFromVersionJson() throws Exception {
        JsonFile versionJson = new JsonFile(Path.of("/home/dervarex/Development/tmp/jsonfile/26-2-pre-2.json"));

        assertEquals(25, JavaManager.getRequiredJavaVersion(versionJson));
    }

    @Test
    void currentRuntimePointsToAnExistingExecutable() {
        JavaInstallation installation = JavaManager.currentRuntime();

        assertEquals(JavaPlatform.majorVersion(), installation.majorVersion());
        assertTrue(Files.exists(installation.executable()));
    }
}

