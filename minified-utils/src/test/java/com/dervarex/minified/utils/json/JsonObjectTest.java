package com.dervarex.minified.utils.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonObjectTest {

    @Test
    void writesWhatWasPutInInOrder() {
        JsonObject nested = new JsonObject();
        nested.put("deep", true);

        JsonObject json = new JsonObject();
        json.put("name", "Notch");
        json.put("age", 15);
        json.put("cool", true);
        json.putNull("job");
        json.put("say \"hi\"", nested);

        assertEquals("{\"name\":\"Notch\",\"age\":15,\"cool\":true,\"job\":null,\"say \\\"hi\\\"\":{\"deep\":true}}", json.toJson());
        assertEquals(json.toJson(), JsonParser.parse(json.toJson()).toJson());
    }

    @Test
    void typedGettersReturnNullForMissingAndNullValues() {
        JsonObject json = JsonParser.parse("{ \"nothing\": null }").asObject();

        for (String key : new String[]{"nothing", "missing"}) {
            assertNull(json.getString(key));
            assertNull(json.getInt(key));
            assertNull(json.getBoolean(key));
            assertNull(json.getObject(key));
            assertNull(json.getArray(key));
        }
    }

    @Test
    void wrongTypesAreNotSilentlyConverted() {
        JsonObject json = JsonParser.parse("{ \"number\": 1 }").asObject();

        assertThrows(IllegalStateException.class, () -> json.getString("number"));
        assertThrows(IllegalStateException.class, () -> json.getObject("number"));
    }

    @Test
    void sameContentMeansEqual() {
        assertEquals(JsonParser.parse("{\"a\": [1, {\"b\": true}]}"), JsonParser.parse("{ \"a\" : [ 1, { \"b\" : true } ] }"));
    }
}
