package com.dervarex.minified.utils.version;

import com.dervarex.minified.utils.ApiEndpoints;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionMetadataProviderTest {

    @Test
    void versionManifestEndpointLooksValid() {
        assertTrue(ApiEndpoints.VERSION_MANIFEST_URL.startsWith("https://"));
        assertTrue(ApiEndpoints.VERSION_MANIFEST_URL.contains("version_manifest"));
    }

    @Test
    void resolvesKnownVersionJsonUrl() throws Exception {
        String url = VersionMetadataProvider.getVersionJsonUrl("1.21.11");

        assertNotNull(url);
        assertTrue(url.startsWith("https://"));
        assertTrue(url.endsWith(".json"));
    }

    @Test
    void readsTheVersionJson() throws Exception {
        assertEquals("net.minecraft.client.main.Main", VersionMetadataProvider.getMainClass("1.21.11"));
        assertEquals("release", VersionMetadataProvider.getVersionType("1.21.11"));
        assertTrue(VersionMetadataProvider.getClientUrl("1.21.11").endsWith("/client.jar"));
        assertTrue(VersionMetadataProvider.getServerUrl("1.21.11").endsWith("/server.jar"));
        assertTrue(VersionMetadataProvider.getClientSha1("1.21.11").matches("[0-9a-f]{40}"));
        assertTrue(VersionMetadataProvider.getServerSha1("1.21.11").matches("[0-9a-f]{40}"));
        assertTrue(VersionMetadataProvider.getReleaseTime("1.21.11").startsWith("2025-"));
    }

    @Test
    void unknownVersionsGiveNull() throws Exception {
        assertNull(VersionMetadataProvider.getVersionJsonUrl("1.99.99"));
        assertNull(VersionMetadataProvider.getClientUrl("1.99.99"));
    }
}
