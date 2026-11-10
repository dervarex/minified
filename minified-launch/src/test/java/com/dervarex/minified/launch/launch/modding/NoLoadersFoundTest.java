package com.dervarex.minified.launch.launch.modding;

import com.dervarex.minified.launch.exceptions.loader.NoLoadersFoundException;
import com.dervarex.minified.launch.launch.modding.fabric.FabricLoaderFetcher;
import com.dervarex.minified.launch.launch.modding.forge.api.ForgeVersionFetcher;
import com.dervarex.minified.launch.launch.modding.neoforge.api.NeoVersionFetcher;
import com.dervarex.minified.launch.launch.modding.quilt.QuiltLoaderFetcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoLoadersFoundTest {

    private static final String UNKNOWN_MINECRAFT = "1.99";

    @Test
    void forgeLatest() {
        NoLoadersFoundException e = assertThrows(NoLoadersFoundException.class,
                () -> new ForgeVersionFetcher().getLatest(UNKNOWN_MINECRAFT));
        assertEquals("FORGE", e.getLoader());
    }

    @Test
    void forgeRecommended() {
        assertThrows(NoLoadersFoundException.class, () -> new ForgeVersionFetcher().getRecommended(UNKNOWN_MINECRAFT));
    }

    @Test
    void fabricProfile() {
        NoLoadersFoundException e = assertThrows(NoLoadersFoundException.class,
                () -> FabricLoaderFetcher.getProfileJson(UNKNOWN_MINECRAFT, FabricLoaderFetcher.getLatestLoaderVersion()));
        assertEquals("FABRIC", e.getLoader());
    }

    @Test
    void quiltLatest() {
        NoLoadersFoundException e = assertThrows(NoLoadersFoundException.class,
                () -> QuiltLoaderFetcher.getLatestLoaderVersion(UNKNOWN_MINECRAFT));
        assertEquals("QUILT", e.getLoader());
    }

    @Test
    void quiltProfile() {
        assertThrows(NoLoadersFoundException.class, () -> QuiltLoaderFetcher.getProfileJson(UNKNOWN_MINECRAFT, "0.28.0"));
    }

    @Test
    void neoforgeLatest() {
        NoLoadersFoundException e = assertThrows(NoLoadersFoundException.class,
                () -> new NeoVersionFetcher().getLatest(UNKNOWN_MINECRAFT));
        assertEquals("NEOFORGE", e.getLoader());
    }
}
