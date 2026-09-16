package com.dervarex.minified.auth.skin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SkinTest {
    static Stream<Arguments> equalsCases() {
        return Stream.of(
                Arguments.of("same fields",
                        new Skin("url", SkinModel.SLIM, "cape"),
                        new Skin("url", SkinModel.SLIM, "cape"), true),
                Arguments.of("different skinUrl",
                        new Skin("url1", SkinModel.SLIM, "cape"),
                        new Skin("url2", SkinModel.SLIM, "cape"), false),
                Arguments.of("different model",
                        new Skin("url", SkinModel.SLIM, "cape"),
                        new Skin("url", SkinModel.CLASSIC, "cape"), false),
                Arguments.of("different capeUrl",
                        new Skin("url", SkinModel.SLIM, "cape1"),
                        new Skin("url", SkinModel.SLIM, "cape2"), false),
                Arguments.of("both null fields",
                        new Skin(null, SkinModel.SLIM, null),
                        new Skin(null, SkinModel.SLIM, null), true),
                Arguments.of("one null field",
                        new Skin(null, SkinModel.SLIM, "cape"),
                        new Skin("url", SkinModel.SLIM, "cape"), false)
        );
    }

    @ParameterizedTest(name = "{0} -> {3}")
    @MethodSource("equalsCases")
    void equals_variousCases(String description, Skin a, Skin b, boolean expected) {
        assertEquals(expected, a.equals(b));
    }

    @Test
    void equals_null_returnsFalse() {
        assertFalse(new Skin("url", SkinModel.SLIM, "cape").equals(null));
    }

    @Test
    void equals_differentType_returnsFalse() {
        assertFalse(new Skin("url", SkinModel.SLIM, "cape").equals("string"));
    }
}
