package com.dervarex.minified.utils.json;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonArrayTest {

    @Test
    void addAndGetEveryType() {
        JsonArray array = new JsonArray();
        array.add("a");
        array.add(1);
        array.add(true);
        array.addNull();

        assertEquals("[\"a\",1,true,null]", array.toJson());
        assertEquals("a", array.getString(0));
        assertEquals(BigDecimal.ONE, array.getNumber(1));
        assertTrue(array.getBoolean(2));
        assertNull(array.getString(3));
    }

    @Test
    void valuesCannotBeChangedFromOutside() {
        JsonArray array = new JsonArray();
        array.add("a");

        assertThrows(UnsupportedOperationException.class, () -> array.values().add(new JsonString("b")));
    }

    @Test
    void nullsComeBackAsNullForEveryGetter() {
        JsonArray array = JsonParser.parse("[null]").asArray();

        assertNull(array.getObject(0));
        assertNull(array.getArray(0));
    }
}
