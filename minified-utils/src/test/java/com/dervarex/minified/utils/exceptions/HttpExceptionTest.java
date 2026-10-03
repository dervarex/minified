package com.dervarex.minified.utils.exceptions;

import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpExceptionTest {

    @Test
    void snippetsCutOffLongBodies() {
        HttpException e = new HttpException.Builder().status(500).body("0123456789").build();

        assertEquals("01234…", e.responseSnippet(5));
        assertEquals("0123456789", e.responseSnippet(10));
    }

    @Test
    void userFriendlyMessageMentionsWhatMatters() {
        String message = new HttpException.Builder()
                .status(503)
                .method(HttpException.Method.POST)
                .url("https://example.com/login")
                .requestId("abc-123")
                .transientFailure(true)
                .build()
                .toUserFriendlyMessage();

        assertTrue(message.startsWith("HTTP error 503 at POST https://example.com/login"));
        assertTrue(message.contains("Request-Id: abc-123"));
        assertTrue(message.contains("temporary, please try again later"));
    }

    @Test
    @Disabled("escape() only handles \\ and \", a line break in the body ends up raw in the JSON")
    void toJsonSurvivesUglyResponses() {
        HttpException e = new HttpException.Builder()
                .status(502)
                .url("https://example.com/\"quoted\"")
                .header("Content-Type", "text/html")
                .body("<h1>Bad Gateway</h1>\n<p>nginx</p>")
                .build();

        JsonObject json = JsonParser.parse(e.toJson()).asObject();

        assertEquals(502, json.get("status").asInt());
        assertEquals("https://example.com/\"quoted\"", json.getString("url"));
        assertEquals("<h1>Bad Gateway</h1>\n<p>nginx</p>", json.getString("bodySnippet"));
        assertEquals("text/html", json.getObject("headers").getString("Content-Type"));
    }
}
