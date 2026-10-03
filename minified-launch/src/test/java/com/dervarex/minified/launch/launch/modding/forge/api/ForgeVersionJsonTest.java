package com.dervarex.minified.launch.launch.modding.forge.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForgeVersionJsonTest {

    @Test
    void versionIdKeepsTheBranchSuffix() {
        assertEquals("1.7.10-forge-10.13.4.1614-1.7.10", ForgeVersionJson.getVersionId("1.7.10-10.13.4.1614-1.7.10"));
        assertEquals("1.21.11-forge-61.1.8", ForgeVersionJson.getVersionId("1.21.11-61.1.8"));
    }
}
