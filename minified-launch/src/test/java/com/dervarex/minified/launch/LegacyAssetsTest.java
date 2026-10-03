package com.dervarex.minified.launch;

import com.dervarex.minified.launch.download.assets.LegacyAssets;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import com.dervarex.minified.launch.launch.modding.vanilla.VanillaLoader;
import com.dervarex.minified.utils.json.JsonFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LegacyAssetsTest {

    private static final String HASH = "bdf48ef6b5d0d23bbb02e17d04865216179f510a";

    @TempDir
    Path tempDir;

    @Test
    void copiesVirtualAssetsUnderTheirNames() throws IOException {
        LaunchConfiguration config = setUp("legacy", "\"virtual\": true,");

        LegacyAssets.reconstruct(versionJson("legacy"), config);

        Path expected = tempDir.resolve("assets/virtual/legacy");
        assertEquals(expected, LegacyAssets.resolveGameAssetsDirectory(versionJson("legacy"), config));
        assertEquals("icon", Files.readString(expected.resolve("icons/icon_16x16.png")));
    }

    @Test
    void copiesPreOneSixAssetsIntoTheResourcesFolder() throws IOException {
        LaunchConfiguration config = setUp("pre-1.6", "\"map_to_resources\": true,");

        LegacyAssets.reconstruct(versionJson("pre-1.6"), config);

        Path expected = tempDir.resolve("jar/resources");
        assertEquals(expected, LegacyAssets.resolveGameAssetsDirectory(versionJson("pre-1.6"), config));
        assertEquals("icon", Files.readString(expected.resolve("icons/icon_16x16.png")));
    }

    @Test
    void leavesNewerVersionsAlone() throws IOException {
        LaunchConfiguration config = setUp("1.12", "");

        LegacyAssets.reconstruct(versionJson("1.12"), config);

        assertEquals(tempDir.resolve("assets"), LegacyAssets.resolveGameAssetsDirectory(versionJson("1.12"), config));
        assertFalse(Files.exists(tempDir.resolve("assets/virtual")));
        assertFalse(Files.exists(tempDir.resolve("jar/resources")));
    }

    private LaunchConfiguration setUp(String indexId, String flags) throws IOException {
        Path index = tempDir.resolve("assets/indexes/" + indexId + ".json");
        Files.createDirectories(index.getParent());
        Files.writeString(index, """
                { %s "objects": { "icons/icon_16x16.png": { "hash": "%s", "size": 4 } } }
                """.formatted(flags, HASH));

        Path object = tempDir.resolve("assets/objects/" + HASH.substring(0, 2) + "/" + HASH);
        Files.createDirectories(object.getParent());
        Files.writeString(object, "icon");

        return new LaunchConfiguration.Builder()
                .assetsDirectory(tempDir.resolve("assets"))
                .librariesDirectory(tempDir.resolve("jar/libraries"))
                .jarFile(tempDir.resolve("jar/client.jar"))
                .loader(new VanillaLoader("1.5.2"))
                .build();
    }

    private static JsonFile versionJson(String indexId) {
        return new JsonFile("{ \"assetIndex\": { \"id\": \"" + indexId + "\" } }");
    }
}
