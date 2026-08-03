package com.dervarex.minified.launch;

import com.dervarex.minified.utils.ApiEndpoints;
import com.dervarex.minified.utils.version.VersionMetadataProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionProviderTest {

    @Test
    void versionManifestEndpointLooksValid() {
        assertTrue(ApiEndpoints.VERSION_MANIFEST_URL.startsWith("https://"));
        assertTrue(ApiEndpoints.VERSION_MANIFEST_URL.contains("version_manifest"));
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
