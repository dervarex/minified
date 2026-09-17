package com.dervarex.minified.modrinth.loaders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShaderLoaderTest {

    @Test
    void getApiValue_returnsExpectedValue() {
        assertEquals("iris", ShaderLoader.IRIS.getApiValue());
        assertEquals("optifine", ShaderLoader.OPTIFINE.getApiValue());
        assertEquals("vanilla", ShaderLoader.VANILLA_SHADER.getApiValue());
        assertEquals("canvas", ShaderLoader.CANVAS.getApiValue());
    }

    @Test
    void fromApiValue_returnsEnum_whenValueMatches() {
        assertEquals(ShaderLoader.IRIS, ShaderLoader.fromApiValue("iris"));
    }

    @Test
    void fromApiValue_ignoresCase() {
        assertEquals(ShaderLoader.OPTIFINE, ShaderLoader.fromApiValue("OPTIFINE"));
    }

    @Test
    void fromApiValue_returnsNull_whenValueIsNull() {
        assertNull(ShaderLoader.fromApiValue(null));
    }

    @Test
    void fromApiValue_throws_whenValueUnknown() {
        assertThrows(IllegalArgumentException.class, () -> ShaderLoader.fromApiValue("unknown"));
    }

    @Test
    void toString_returnsApiValue() {
        assertEquals("iris", ShaderLoader.IRIS.toString());
    }
}