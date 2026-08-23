package com.dervarex.minified.utils.json;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class JsonFileTest {

    @Test
    void readsJsonFileAndExposesValues() throws IOException {
        String json = "{\n" +
                "  \"id\": 10245,\n" +
                "  \"username\": \"coder_99\",\n" +
                "  \"email\": \"coder_99@example.com\",\n" +
                "  \"is_active\": true,\n" +
                "  \"skills\": [\"Python\", \"JavaScript\", \"JSON\"],\n" +
                "  \"address\": {\"city\": \"Frankfurt\", \"country\": \"Germany\"},\n" +
                "  \"subscription_tier\": null\n" +
                "}";

        Path path = Files.createTempFile("minified-json", ".json");
        Files.writeString(path, json, StandardCharsets.UTF_8);

        JsonFile file = new JsonFile(path);
        assertEquals(10245, file.getNumber("id").intValue());
        assertEquals("coder_99", file.getString("username"));
        assertEquals(true, file.getBoolean("is_active"));

        JsonObject address = file.getObject("address");
        assertNotNull(address);
        assertEquals("Frankfurt", address.getString("city"));

        JsonArray skills = file.getArray("skills");
        assertNotNull(skills);
        assertFalse(skills.values().isEmpty());
        assertEquals("Python", skills.getString(0));
    }
}

