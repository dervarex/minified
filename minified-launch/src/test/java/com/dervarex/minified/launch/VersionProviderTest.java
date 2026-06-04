package com.dervarex.minified.launch;

import com.dervarex.minified.launch.version.VersionMetadataProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionProviderTest {

    @Test
    void versionManifestEndpointLooksValid() {
        assertTrue(ApiEndpoints.VersionManifestUrl.startsWith("https://"));
        assertTrue(ApiEndpoints.VersionManifestUrl.contains("version_manifest"));
    }

    @Tag("integration")
    @Test
    @EnabledIfEnvironmentVariable(named = "MINIFIED_VERSION_IT", matches = "(?i)1|true|yes")
    void resolvesKnownVersionJsonUrlWhenIntegrationTestsAreEnabled() throws Exception {
        String url = VersionMetadataProvider.getVersionJsonUrl("1.21.11");

        assertNotNull(url);
        assertTrue(url.startsWith("https://"));
        assertTrue(url.endsWith(".json"));
    }
}
