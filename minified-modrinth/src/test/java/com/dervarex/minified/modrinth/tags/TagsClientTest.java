package com.dervarex.minified.modrinth.tags;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.projects.ProjectType;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class TagsClientTest {

    private static TagsClient newClient() {
        return new TagsClient(new Modrinth("http://test"));
    }

    private static Tag parseTag(JsonObject object) throws Exception {
        Method method = TagsClient.class.getDeclaredMethod("parseTag", JsonObject.class);
        method.setAccessible(true);
        return (Tag) method.invoke(newClient(), object);
    }

    private static License parseLicense(JsonObject object) throws Exception {
        // I was too lazy to make TagsClient not final, so we just do reflection
        Method method = TagsClient.class.getDeclaredMethod("parseLicense", JsonObject.class);
        method.setAccessible(true);
        return (License) method.invoke(newClient(), object);
    }

    @Test
    void parseTag_parsesAllFields() throws Exception {
        JsonObject obj = new JsonObject();
        obj.put("id", "combat");
        obj.put("name", "Combat");
        obj.put("description", "Fight stuff");
        obj.put("icon", "icon.png");
        obj.put("header", "header.png");
        obj.put("value", "combat_value");
        obj.put("featured", true);
        obj.put("project_type", "mod");

        Tag tag = parseTag(obj);
        assertEquals("combat", tag.getId());
        assertEquals("Combat", tag.getName());
        assertEquals("Fight stuff", tag.getDescription());
        assertEquals("icon.png", tag.getIcon());
        assertEquals("header.png", tag.getHeader());
        assertEquals("combat_value", tag.getValue());
        assertTrue(tag.isFeatured());
        assertEquals(ProjectType.MOD, tag.getProjectType());
    }

    @Test
    void parseTag_featuredFalse_whenFlagFalse() throws Exception {
        JsonObject obj = new JsonObject();
        obj.put("featured", false);
        assertFalse(parseTag(obj).isFeatured());
    }

    @Test
    void parseTag_featuredFalse_whenFlagMissing() throws Exception {
        JsonObject obj = new JsonObject();
        assertFalse(parseTag(obj).isFeatured());
    }

    @Test
    void parseTag_parsesProjectTypes() throws Exception {
        JsonObject obj = new JsonObject();
        JsonArray types = new JsonArray();
        types.add("mod");
        types.add("modpack");
        obj.put("project_types", types);
        assertArrayEquals(new String[]{"mod", "modpack"}, parseTag(obj).getProjectTypes());
    }

    @Test
    void parseTag_returnsNull_whenObjectNull() throws Exception {
        assertNull(parseTag(null));
    }

    @Test
    void parseLicense_parsesAllFields() throws Exception {
        JsonObject obj = new JsonObject();
        obj.put("id", "mit");
        obj.put("name", "MIT License");
        obj.put("url", "https://example.com/mit");

        License license = parseLicense(obj);
        assertEquals("mit", license.getId());
        assertEquals("MIT License", license.getName());
        assertEquals("https://example.com/mit", license.getUrl());
    }

    @Test
    void parseLicense_returnsNull_whenObjectNull() throws Exception {
        assertNull(parseLicense(null));
    }
}