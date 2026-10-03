package com.dervarex.minified.launch.launch.internal;

import com.dervarex.minified.java.JavaManager;
import com.dervarex.minified.utils.exceptions.OfflineModeNeedsNetworkException;
import com.dervarex.minified.utils.json.JsonFile;
import com.dervarex.minified.utils.json.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class CacheManagerTest {

    @TempDir
    Path tempDir;

    private Path previousBaseDir;

    @BeforeEach
    void redirectBaseDir() {
        previousBaseDir = JavaManager.getBaseDir();
        JavaManager.init(tempDir);
    }

    @AfterEach
    void restoreBaseDir() {
        JavaManager.init(previousBaseDir);
    }

    @Test
    void fetchesProfilesOnceAndCachesThem() {
        JsonObject fetched = new JsonFile("{ \"id\": \"fabric-loader-0.16.14-1.21.11\" }").asObject();

        CacheManager.loadProfileJson("1.21.11", "fabric", "0.16.14", true, () -> fetched);
        JsonObject cached = CacheManager.loadProfileJson("1.21.11", "fabric", "0.16.14", false,
                () -> fail("should have used the cache"));

        assertEquals("fabric-loader-0.16.14-1.21.11", cached.get("id").asString());
    }

    @Test
    void refetchesBrokenProfiles() throws IOException {
        Path cached = Files.createDirectories(tempDir.resolve("cache/profiles/fabric/1.21.11")).resolve("0.16.14.json");
        Files.writeString(cached, "{ this is not json");

        JsonObject profile = CacheManager.loadProfileJson("1.21.11", "fabric", "0.16.14", true,
                () -> new JsonFile("{ \"id\": \"fresh\" }").asObject());

        assertEquals("fresh", profile.get("id").asString());
    }

    @Test
    void offlineWithoutCacheNeedsTheNetwork() {
        OfflineModeNeedsNetworkException profile = assertThrows(OfflineModeNeedsNetworkException.class,
                () -> CacheManager.loadProfileJson("1.21.11", "fabric", "0.16.14", false, () -> fail("we are offline")));
        OfflineModeNeedsNetworkException version = assertThrows(OfflineModeNeedsNetworkException.class,
                () -> CacheManager.loadVersionJson("1.21.11", false));

        assertEquals(OfflineModeNeedsNetworkException.Reason.MISSING_LOADER_PROFILE, profile.getReason());
        assertEquals(OfflineModeNeedsNetworkException.Reason.MISSING_VERSION_MANIFEST, version.getReason());
    }

    @Test
    void usesCachedVersionJsonOffline() throws IOException {
        Path cached = Files.createDirectories(tempDir.resolve("cache/versions")).resolve("1.21.11.json");
        Files.writeString(cached, "{ \"id\": \"1.21.11\" }");

        assertEquals("1.21.11", CacheManager.loadVersionJson("1.21.11", false).get("id").asString());
    }
}
