package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.forge.ForgeLoader;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.os.OS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClasspathBuilderTest {

    private static final String VANILLA_JSON = """
            {
              "id": "1.21.4",
              "libraries": [
                { "name": "org.ow2.asm:asm:9.6", "downloads": { "artifact": { "path": "org/ow2/asm/asm/9.6/asm-9.6.jar" } } },
                { "name": "org.apache.commons:commons-lang3:3.17.0", "downloads": { "artifact": { "path": "org/apache/commons/commons-lang3/3.17.0/commons-lang3-3.17.0.jar" } } },
                { "name": "com.mojang:brigadier:1.3.10", "downloads": { "artifact": { "path": "com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar" } } }
              ]
            }
            """;

    @TempDir
    Path tempDir;

    @Test
    void loaderLibrariesComeFirstAndReplaceVanillaVersions() throws IOException {
        List<String> classpath = buildForgeClasspath("[]");

        assertEquals(List.of(
                library("net/minecraftforge/forge/1.21.4-54.1.0/forge-1.21.4-54.1.0-client.jar"),
                library("org/ow2/asm/asm/9.8/asm-9.8.jar"),
                library("org/apache/commons/commons-lang3/3.17.0/commons-lang3-3.17.0.jar"),
                library("com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar"),
                tempDir.resolve("jar/client.jar").toAbsolutePath().toString()
        ), classpath);
    }

    @Test
    void leavesOutClientJarIgnoredByTheLoader() throws IOException {
        List<String> classpath = buildForgeClasspath("[\"-DignoreList=client-extra,${version_name}.jar\"]");

        assertFalse(classpath.contains(tempDir.resolve("jar/client.jar").toAbsolutePath().toString()));
    }

    @Test
    void vanillaOnlyTakesLibrariesItCanUse() {
        String versionJson = """
                {
                  "id": "1.21.11",
                  "libraries": [
                    { "name": "org.ow2.asm:asm:9.6", "downloads": { "artifact": { "path": "org/ow2/asm/asm/9.6/asm-9.6.jar" } } },
                    { "name": "org.lwjgl:lwjgl:3.3.3:natives-linux" },
                    { "name": "org.lwjgl.lwjgl:lwjgl-platform:2.9.4", "natives": { "linux": "natives-linux" } },
                    { "name": "some:other-os-only:1.0", "rules": [{ "action": "allow", "os": { "name": "%s" } }] },
                    { "name": "some:server-only:1.0", "clientreq": false }
                  ]
                }
                """.formatted(OS.getCurrentOS() == OS.WINDOWS ? "linux" : "windows");

        assertEquals(List.of(
                tempDir.resolve("libraries/org/ow2/asm/asm/9.6/asm-9.6.jar").toString(),
                tempDir.resolve("libraries/org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-linux.jar").toString(),
                tempDir.resolve("jar/client.jar").toString()
        ), buildVanillaClasspath(versionJson));
    }

    @Test
    void prefersLibrariesTheInstallerPutIntoTheGameDirectory() throws IOException {
        Path installed = Files.createDirectories(tempDir.resolve("jar/libraries/org/ow2/asm/asm/9.6")).resolve("asm-9.6.jar");
        Files.createFile(installed);

        assertEquals(installed.toString(), buildVanillaClasspath(VANILLA_JSON).getFirst());
    }

    private List<String> buildVanillaClasspath(String versionJson) {
        LaunchConfiguration config = new LaunchConfiguration.Builder()
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("libraries"))
                .jarFile(tempDir.resolve("jar/client.jar"))
                .loader(new VanillaLoader("1.21.11"))
                .build();

        return List.of(ClasspathBuilder.buildClasspath(new JsonFile(versionJson), config, false).split(File.pathSeparator));
    }

    private List<String> buildForgeClasspath(String jvmArguments) throws IOException {
        Path profile = tempDir.resolve("jar/versions/1.21.4-forge-54.1.0/1.21.4-forge-54.1.0.json");
        Files.createDirectories(profile.getParent());
        Files.writeString(profile, """
                {
                  "id": "1.21.4-forge-54.1.0",
                  "arguments": { "jvm": %s },
                  "libraries": [
                    { "name": "net.minecraftforge:forge:1.21.4-54.1.0:client", "downloads": { "artifact": { "path": "net/minecraftforge/forge/1.21.4-54.1.0/forge-1.21.4-54.1.0-client.jar" } } },
                    { "name": "org.ow2.asm:asm:9.8", "downloads": { "artifact": { "path": "org/ow2/asm/asm/9.8/asm-9.8.jar" } } },
                    { "name": "org.apache.commons:commons-lang3:3.17.0", "downloads": { "artifact": { "path": "org/apache/commons/commons-lang3/3.17.0/commons-lang3-3.17.0.jar" } } },
                    { "name": "org.scala-lang:scala-compiler:2.11.1", "serverreq": true, "clientreq": false }
                  ]
                }
                """.formatted(jvmArguments));

        LaunchConfiguration config = new LaunchConfiguration.Builder()
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("jar/libraries"))
                .jarFile(tempDir.resolve("jar/client.jar"))
                .loader(new ForgeLoader("1.21.4", "1.21.4-54.1.0"))
                .build();

        String classpath = ClasspathBuilder.buildClasspath(new JsonFile(VANILLA_JSON), config, false);
        return List.of(classpath.split(File.pathSeparator));
    }

    private String library(String relativePath) {
        return tempDir.resolve("jar/libraries").resolve(relativePath).toAbsolutePath().toString();
    }
}
