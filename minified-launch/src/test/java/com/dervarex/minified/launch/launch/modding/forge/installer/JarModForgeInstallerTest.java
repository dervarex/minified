package com.dervarex.minified.launch.launch.modding.forge.installer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JarModForgeInstallerTest {

    @TempDir
    Path tempDir;

    @Test
    void readsLibrariesFromFml() throws IOException {
        // the same shape as CoreFMLLibraries of FML for 1.4.7
        Path jarMod = jarModWith("""
                package cpw.mods.fml.relauncher;

                public class CoreFMLLibraries {
                    private static String[] libraries = { "argo-2.25.jar", "guava-12.0.1.jar" };
                    private static String[] checksums = {
                            "bb672829fde76cb163004752b86b0484bd0a7f4b",
                            "b8e78b9af7bf45900e14c6f958486b6ca682195f"
                    };

                    public String getRootURL() {
                        return "http://files.minecraftforge.net/fmllibs/%s";
                    }
                }
                """);

        List<String[]> libraries = JarModForgeInstaller.readFmlLibraries(jarMod);

        assertEquals(2, libraries.size());
        assertArrayEquals(new String[]{"argo-2.25.jar", "bb672829fde76cb163004752b86b0484bd0a7f4b"}, libraries.get(0));
        assertArrayEquals(new String[]{"guava-12.0.1.jar", "b8e78b9af7bf45900e14c6f958486b6ca682195f"}, libraries.get(1));
    }

    @Test
    void jarModsWithoutFmlNeedNoLibraries() throws IOException {
        Path jarMod = tempDir.resolve("forge-client.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jarMod))) {
            out.putNextEntry(new ZipEntry("forge/ForgeHooks.class"));
            out.closeEntry();
        }

        assertTrue(JarModForgeInstaller.readFmlLibraries(jarMod).isEmpty());
    }

    private Path jarModWith(String coreFmlLibrariesSource) throws IOException {
        Path source = tempDir.resolve("src/cpw/mods/fml/relauncher/CoreFMLLibraries.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, coreFmlLibrariesSource);

        Path classes = tempDir.resolve("classes");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertEquals(0, compiler.run(null, null, null, "-d", classes.toString(), source.toString()));

        Path jarMod = tempDir.resolve("forge-universal.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jarMod))) {
            out.putNextEntry(new ZipEntry("cpw/mods/fml/relauncher/CoreFMLLibraries.class"));
            out.write(Files.readAllBytes(classes.resolve("cpw/mods/fml/relauncher/CoreFMLLibraries.class")));
            out.closeEntry();
        }
        return jarMod;
    }
}
