package com.dervarex.minified.utils.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonParserTest {

    @Test
    void parsesEveryKindOfValue() {
        JsonObject json = JsonParser.parse("""
                {
                  "string": "hi", "int": -42, "decimal": 0.1, "exponent": 1.5e3,
                  "yes": true, "no": false, "nothing": null,
                  "array": [1, "two", [3]], "object": { "nested": {} }
                }
                """).asObject();

        assertEquals("hi", json.getString("string"));
        assertEquals(-42, json.getInt("int"));
        assertEquals(new BigDecimal("0.1"), json.getNumber("decimal"));
        assertEquals(1500, json.getInt("exponent"));
        assertTrue(json.getBoolean("yes"));
        assertFalse(json.getBoolean("no"));
        assertTrue(json.get("nothing").isNull());
        assertEquals(3, json.getArray("array").size());
        assertEquals(0, json.getObject("object").getObject("nested").size());
    }

    @Test
    void understandsEscapes() {
        assertEquals("\"\\/\b\f\n\r\t é 😀",
                JsonParser.parse("\"\\\"\\\\\\/\\b\\f\\n\\r\\t \\u00e9 \\ud83d\\ude00\"").asString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{", "[1,]", "{\"a\" 1}", "{a:1}", "'single'", "01", "-", "tru", "{} {}",
            "\"unterminated", "\"bad \\x escape\"", "\"raw \n newline\""})
    void rejectsBrokenJson(String input) {
        assertThrows(JsonParseException.class, () -> JsonParser.parse(input));
    }

    @Test
    void saysWhereItBroke() {
        assertEquals(5, assertThrows(JsonParseException.class, () -> JsonParser.parse("[1, 2")).getPosition());
    }
}
