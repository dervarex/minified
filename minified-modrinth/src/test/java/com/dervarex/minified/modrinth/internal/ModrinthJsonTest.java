package com.dervarex.minified.modrinth.internal;

import com.dervarex.minified.modrinth.exceptions.ModrinthSerializationException;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ModrinthJsonTest {

    enum TestEnum { A, B }

    @Test
    void string_returnsValue_whenKeyExists() {
        JsonObject obj = new JsonObject();
        obj.put("name", "test");
        assertEquals("test", ModrinthJson.string(obj, "name"));
    }

    @Test
    void string_returnsNull_whenKeyMissing() {
        JsonObject obj = new JsonObject();
        assertNull(ModrinthJson.string(obj, "missing"));
    }

    @Test
    void string_returnsNull_whenValueIsNull() {
        JsonObject obj = new JsonObject();
        obj.putNull("name");
        assertNull(ModrinthJson.string(obj, "name"));
    }

    @Test
    void string_returnsNull_whenObjectIsNull() {
        assertNull(ModrinthJson.string(null, "name"));
    }

    @Test
    void bool_returnsTrue_whenValueIsTrue() {
        JsonObject obj = new JsonObject();
        obj.put("flag", true);
        assertTrue(ModrinthJson.bool(obj, "flag"));
    }

    @Test
    void bool_returnsFalse_whenValueIsFalse() {
        JsonObject obj = new JsonObject();
        obj.put("flag", false);
        assertFalse(ModrinthJson.bool(obj, "flag"));
    }

    @Test
    void bool_returnsNull_whenKeyMissing() {
        JsonObject obj = new JsonObject();
        assertNull(ModrinthJson.bool(obj, "missing"));
    }

    @Test
    void bool_returnsNull_whenObjectIsNull() {
        assertNull(ModrinthJson.bool(null, "flag"));
    }

    @Test
    void longValue_returnsLong_whenNumber() {
        JsonObject obj = new JsonObject();
        obj.put("count", 123L);
        assertEquals(123L, ModrinthJson.longValue(obj, "count"));
    }

    @Test
    void longValue_returnsNull_whenKeyMissing() {
        JsonObject obj = new JsonObject();
        assertNull(ModrinthJson.longValue(obj, "missing"));
    }

    @Test
    void integer_returnsInteger_whenNumber() {
        JsonObject obj = new JsonObject();
        obj.put("count", 123);
        assertEquals(123, ModrinthJson.integer(obj, "count"));
    }

    @Test
    void integer_returnsNull_whenKeyMissing() {
        JsonObject obj = new JsonObject();
        assertNull(ModrinthJson.integer(obj, "missing"));
    }

    @Test
    void instant_parsesInstant_whenValidString() {
        JsonObject obj = new JsonObject();
        obj.put("date", "2024-01-01T00:00:00Z");
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), ModrinthJson.instant(obj, "date"));
    }

    @Test
    void instant_returnsNull_whenBlank() {
        JsonObject obj = new JsonObject();
        obj.put("date", "   ");
        assertNull(ModrinthJson.instant(obj, "date"));
    }

    @Test
    void instant_returnsNull_whenKeyMissing() {
        JsonObject obj = new JsonObject();
        assertNull(ModrinthJson.instant(obj, "missing"));
    }

    @Test
    void strings_returnsArray_whenArrayExists() {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add("a");
        arr.add("b");
        obj.put("list", arr);
        String[] result = ModrinthJson.strings(obj, "list");
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    @Test
    void strings_returnsNull_whenKeyMissing() {
        JsonObject obj = new JsonObject();
        assertNull(ModrinthJson.strings(obj, "missing"));
    }

    @Test
    void object_returnsObject_whenExists() {
        JsonObject obj = new JsonObject();
        JsonObject nested = new JsonObject();
        nested.put("x", 1);
        obj.put("nested", nested);
        assertEquals(nested, ModrinthJson.object(obj, "nested"));
    }

    @Test
    void object_returnsNull_whenKeyMissing() {
        JsonObject obj = new JsonObject();
        assertNull(ModrinthJson.object(obj, "missing"));
    }

    @Test
    void array_returnsArray_whenExists() {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        obj.put("list", arr);
        assertEquals(arr, ModrinthJson.array(obj, "list"));
    }

    @Test
    void array_returnsNull_whenKeyMissing() {
        JsonObject obj = new JsonObject();
        assertNull(ModrinthJson.array(obj, "missing"));
    }

    @Test
    void stringMap_returnsMap_whenObjectExists() {
        JsonObject obj = new JsonObject();
        JsonObject nested = new JsonObject();
        nested.put("a", "1");
        nested.put("b", "2");
        obj.put("map", nested);
        Map<String, String> result = ModrinthJson.stringMap(obj, "map");
        assertEquals(Map.of("a", "1", "b", "2"), result);
    }

    @Test
    void stringMap_returnsNull_whenKeyMissing() {
        JsonObject obj = new JsonObject();
        assertNull(ModrinthJson.stringMap(obj, "missing"));
    }

    @Test
    void parse_returnsParsedValue() {
        JsonObject obj = new JsonObject();
        obj.put("key", "value");
        String result = ModrinthJson.parse(obj, o -> o.getString("key"));
        assertEquals("value", result);
    }

    @Test
    void parse_throwsModrinthSerializationException_whenParserThrows() {
        JsonObject obj = new JsonObject();
        assertThrows(ModrinthSerializationException.class, () ->
                ModrinthJson.parse(obj, o -> { throw new RuntimeException("fail"); })
        );
    }

    @Test
    void enumValue_returnsEnum_whenValidString() {
        JsonObject obj = new JsonObject();
        obj.put("type", "A");
        TestEnum result = ModrinthJson.enumValue(obj, "type", TestEnum::valueOf);
        assertEquals(TestEnum.A, result);
    }

    @Test
    void enumValue_returnsNull_whenKeyMissing() {
        JsonObject obj = new JsonObject();
        assertNull(ModrinthJson.enumValue(obj, "missing", TestEnum::valueOf));
    }

    @Test
    void copyOf_returnsCopy_withoutNulls() {
        String[] input = {"a", null, "b"};
        String[] result = ModrinthJson.copyOf(input);
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    @Test
    void copyOf_returnsNull_whenInputNull() {
        assertNull(ModrinthJson.copyOf(null));
    }
}