package com.dervarex.minified.launch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class LaunchUtilsTest {
    @Test
    void safeArgsFiltersNullAndBlankValues() {
        String[] result = LaunchUtils.safeArgs(new String[]{"  a  ", null, "", "b"});
        assertArrayEquals(new String[]{"a", "b"}, result);
    }
}

