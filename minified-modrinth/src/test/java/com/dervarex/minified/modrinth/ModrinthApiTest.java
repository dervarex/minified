package com.dervarex.minified.modrinth;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModrinthApiTest {
    @Test
    public void testStagingApi() throws HttpException, IOException {
        String response = HttpUtil.get("https://staging-api.modrinth.com/");
        JsonObject json = JsonParser.parse(response).asObject();

        assertEquals("Welcome traveler!", json.get("about").toString());
        assertEquals("https://docs.modrinth.com", json.get("documentation").toString());
        assertEquals("modrinth-labrinth", json.get("name").toString());
        // not testing the version, as it might change over time
    }
}
