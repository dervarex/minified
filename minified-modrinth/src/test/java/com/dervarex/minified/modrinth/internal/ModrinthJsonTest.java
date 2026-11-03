package com.dervarex.minified.modrinth.internal;

import com.dervarex.minified.modrinth.exceptions.ModrinthSerializationException;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ModrinthJsonTest {

    private static JsonObject json(String json) {
        return JsonParser.parse(json).asObject();
    }

    private static final JsonObject SAMPLE = json("""
            {"name":"test","nothing":null,"flag":true,"count":42,"date":"2024-01-01T00:00:00Z","blank":" ",
             "names":["a",null,"b"],"objects":[{"x":1},null,{"x":2}],"hashes":{"sha1":"abc","sha512":null},
             "nested":{"x":1},"array":[1]}""");

    @Test
    void string() {
        assertEquals("test", ModrinthJson.string(SAMPLE, "name"));
        assertNull(ModrinthJson.string(SAMPLE, "missing"));
        assertNull(ModrinthJson.string(SAMPLE, "nothing"));
        assertNull(ModrinthJson.string(null, "name"));
    }

    @Test
    void bool_isFalseWhenMissing() {
        assertTrue(ModrinthJson.bool(SAMPLE, "flag"));
        assertFalse(ModrinthJson.bool(SAMPLE, "missing"));
        assertFalse(ModrinthJson.bool(SAMPLE, "nothing"));
    }

    @Test
    void number_isZeroWhenMissing() {
        assertEquals(42L, ModrinthJson.number(SAMPLE, "count"));
        assertEquals(0L, ModrinthJson.number(SAMPLE, "missing"));
    }

    @Test
    void integer_isNullWhenMissing() {
        assertEquals(42, ModrinthJson.integer(SAMPLE, "count"));
        assertNull(ModrinthJson.integer(SAMPLE, "missing"));
    }

    @Test
    void instant() {
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), ModrinthJson.instant(SAMPLE, "date"));
        assertNull(ModrinthJson.instant(SAMPLE, "blank"));
        assertNull(ModrinthJson.instant(SAMPLE, "missing"));
    }

    @Test
    void strings_skipNullsAndAreEmptyWhenMissing() {
        assertEquals(List.of("a", "b"), ModrinthJson.strings(SAMPLE, "names"));
        assertEquals(List.of(), ModrinthJson.strings(SAMPLE, "missing"));
        assertThrows(UnsupportedOperationException.class, () -> ModrinthJson.strings(SAMPLE, "names").add("c"));
    }

    @Test
    void objects() {
        assertEquals(List.of(1, 2), ModrinthJson.objects(SAMPLE, "objects", object -> object.getInt("x")));
        assertEquals(List.of(), ModrinthJson.objects(SAMPLE, "missing", object -> object));
    }

    @Test
    void list() {
        assertEquals(List.of("A", "B"), ModrinthJson.list(SAMPLE, "names", value -> value.asString().toUpperCase()));
    }

    @Test
    void objectAndArray() {
        assertEquals(1, ModrinthJson.object(SAMPLE, "nested").getInt("x"));
        assertNull(ModrinthJson.object(SAMPLE, "missing"));
        assertEquals(1, ModrinthJson.array(SAMPLE, "array").size());
        assertNull(ModrinthJson.array(SAMPLE, "missing"));
    }

    @Test
    void stringMap_skipsNullValuesAndIsEmptyWhenMissing() {
        assertEquals(Map.of("sha1", "abc"), ModrinthJson.stringMap(SAMPLE, "hashes"));
        assertEquals(Map.of(), ModrinthJson.stringMap(SAMPLE, "missing"));
    }

    @Test
    void parse_wrapsFailures() {
        assertEquals("test", ModrinthJson.parse(SAMPLE, object -> ModrinthJson.string(object, "name")));
        ModrinthSerializationException ex = assertThrows(ModrinthSerializationException.class,
                () -> ModrinthJson.parse(SAMPLE, object -> object.get("name").asObject()));
        assertNotNull(ex.getCause());
    }

    @Test
    void parse_keepsSerializationExceptions() {
        ModrinthSerializationException original = new ModrinthSerializationException("inner", null);
        assertSame(original, assertThrows(ModrinthSerializationException.class, () -> ModrinthJson.parse(SAMPLE, object -> {
            throw original;
        })));
    }
}
