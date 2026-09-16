package com.dervarex.minified.auth.skin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinModelTest {
    // I'll forget how that works tomorrow probably, but it does
    // @CsvSource parses each string as a CSV row, first value goes into "input",
    // second one into "expected"
    @ParameterizedTest
    @CsvSource({
            "slim,    SLIM",
            "SLIM,    SLIM",
            "sLiM,    SLIM",
            "classic, CLASSIC",
            "CLASSIC, CLASSIC",
            "claSsIc, CLASSIC"
    })
    void fromString_isCaseInsensitive(String input, SkinModel expected) {
        assertEquals(expected, SkinModel.fromString(input));
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"not-a-model", "steve", "slim "})
    void fromString_fallsBackToClassic_forNullOrUnknown(String input) {
        assertEquals(SkinModel.CLASSIC, SkinModel.fromString(input));
    }
    @Test
    void getDefault_returnsCorrectModel() {
        assertEquals(SkinModel.CLASSIC, SkinModel.getDefault(new UUID(0L, 0L)));
        assertEquals(SkinModel.SLIM, SkinModel.getDefault(new UUID(0L, 1L)));
    }
}
