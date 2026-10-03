package com.dervarex.minified.utils.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NetworkUtilTest {

    @Test
    void weAreOnline() {
        assertDoesNotThrow(() -> NetworkUtil.ensureOnline("running the tests"));
    }
}
