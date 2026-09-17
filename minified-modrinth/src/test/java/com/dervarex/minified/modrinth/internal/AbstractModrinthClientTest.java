package com.dervarex.minified.modrinth.internal;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.exceptions.ModrinthSerializationException;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AbstractModrinthClientTest {

    static class TestClient extends AbstractModrinthClient {
        private final String response;

        TestClient(Modrinth modrinth, String response) {
            super(modrinth);
            this.response = response;
        }

        @Override
        protected String request(String path, Map<String, String> query) {
            return response;
        }

        public String callBuildUrl(String path, Map<String, String> query) {
            return buildUrl(path, query);
        }

        public Map<String, String> callQuery(Object... entries) {
            return query(entries);
        }

        public JsonObject callReadJsonObject(String json) {
            return readJsonObject(json);
        }

        public JsonArray callReadJsonArray(String json) {
            return readJsonArray(json);
        }

        public static String callEncode(String value) {
            return encode(value);
        }
    }

    private Modrinth modrinth() {
        return new Modrinth("http://test");
    }

    @Test
    void buildUrl_addsLeadingSlash() {
        TestClient client = new TestClient(modrinth(), "");
        assertEquals("http://test/path", client.callBuildUrl("path", Map.of()));
    }

    @Test
    void buildUrl_keepsLeadingSlash() {
        TestClient client = new TestClient(modrinth(), "");
        assertEquals("http://test/path", client.callBuildUrl("/path", Map.of()));
    }

    @Test
    void buildUrl_appendsQuery() {
        TestClient client = new TestClient(modrinth(), "");
        String url = client.callBuildUrl("/path", Map.of("a", "1"));
        assertEquals("http://test/path?a=1", url);
    }

    @Test
    void buildUrl_skipsNullQueryValues() {
        TestClient client = new TestClient(modrinth(), "");
        Map<String, String> query = new java.util.LinkedHashMap<>();
        query.put("a", null);
        query.put("b", "2");
        String url = client.callBuildUrl("/path", query);
        assertEquals("http://test/path?b=2", url);
    }

    @Test
    void buildUrl_encodesQuery() {
        TestClient client = new TestClient(modrinth(), "");
        String url = client.callBuildUrl("/path", Map.of("q", "hello world"));
        assertEquals("http://test/path?q=hello+world", url);
    }

    @Test
    void buildUrl_handlesNullPath() {
        TestClient client = new TestClient(modrinth(), "");
        assertEquals("http://test/", client.callBuildUrl(null, Map.of()));
    }

    @Test
    void query_returnsEmptyMapForNoArgs() {
        TestClient client = new TestClient(modrinth(), "");
        assertTrue(client.callQuery().isEmpty());
    }

    @Test
    void query_throwsForOddArgs() {
        TestClient client = new TestClient(modrinth(), "");
        assertThrows(IllegalArgumentException.class, () -> client.callQuery("key"));
    }

    @Test
    void query_skipsNullKeysAndValues() {
        TestClient client = new TestClient(modrinth(), "");
        Map<String, String> result = client.callQuery("a", "1", null, "2", "b", null);
        assertEquals(Map.of("a", "1"), result);
    }

    @Test
    void encode_encodesSpaces() {
        assertEquals("hello+world", TestClient.callEncode("hello world"));
    }

    @Test
    void readJsonObject_parsesValidJson() {
        TestClient client = new TestClient(modrinth(), "");
        JsonObject obj = client.callReadJsonObject("{\"a\":1}");
        assertEquals(1, obj.getInt("a"));
    }

    @Test
    void readJsonObject_throwsOnInvalidJson() {
        TestClient client = new TestClient(modrinth(), "");
        assertThrows(ModrinthSerializationException.class, () -> client.callReadJsonObject("{bad}"));
    }

    @Test
    void readJsonArray_parsesValidJson() {
        TestClient client = new TestClient(modrinth(), "");
        JsonArray arr = client.callReadJsonArray("[1,2]");
        assertEquals(2, arr.size());
    }

    @Test
    void readJsonArray_throwsOnInvalidJson() {
        TestClient client = new TestClient(modrinth(), "");
        assertThrows(ModrinthSerializationException.class, () -> client.callReadJsonArray("[bad]"));
    }

    @Test
    void getObject_returnsObject() {
        TestClient client = new TestClient(modrinth(), "{\"a\":1}");
        JsonObject obj = client.getObject("/path");
        assertEquals(1, obj.getInt("a"));
    }

    @Test
    void getArray_returnsArray() {
        TestClient client = new TestClient(modrinth(), "[1,2]");
        JsonArray arr = client.getArray("/path");
        assertEquals(2, arr.size());
    }

    @Test
    void getRaw_returnsString() {
        TestClient client = new TestClient(modrinth(), "hello");
        assertEquals("hello", client.getRaw("/path", Map.of()));
    }

    @Test
    void getList_parsesArray() {
        TestClient client = new TestClient(modrinth(), "[{\"x\":1},{\"x\":2}]");
        List<JsonObject> list = client.getList("/path", Map.of(), obj -> obj);
        assertEquals(2, list.size());
        assertEquals(1, list.get(0).getInt("x"));
        assertEquals(2, list.get(1).getInt("x"));
    }

    @Test
    void getList_skipsNullElements() {
        TestClient client = new TestClient(modrinth(), "[{\"x\":1},null,{\"x\":2}]");
        List<JsonObject> list = client.getList("/path", Map.of(), obj -> obj);
        assertEquals(2, list.size());
    }
}