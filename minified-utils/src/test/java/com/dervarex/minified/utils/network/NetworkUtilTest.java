package com.dervarex.minified.utils.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkUtilTest {

    @Test
    void weAreOnline() {
        assertDoesNotThrow(() -> NetworkUtil.ensureOnline("running the tests"));
    }

    @Test
    void asksTheInternetOnlyOnceInAWhile() throws Exception {
        NetworkUtil.ensureOnline("first");

        long start = System.nanoTime();
        NetworkUtil.ensureOnline("second");

        assertTrue(System.nanoTime() - start < 50_000_000L);
    }
}
