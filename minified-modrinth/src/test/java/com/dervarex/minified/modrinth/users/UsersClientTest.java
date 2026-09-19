package com.dervarex.minified.modrinth.users;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.utils.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UsersClientTest {

    private static UsersClient newClient() {
        return new UsersClient(new Modrinth("http://test"));
    }

    private static User parseUser(JsonObject object) throws Exception {
        Method method = UsersClient.class.getDeclaredMethod("parseUser", JsonObject.class);
        method.setAccessible(true);
        return (User) method.invoke(newClient(), object);
    }

    private static String toJsonArray(String... values) throws Exception {
        Method method = UsersClient.class.getDeclaredMethod("toJsonArray", String[].class);
        method.setAccessible(true);
        return (String) method.invoke(null, (Object) values);
    }

    @Test
    void parseUser_parsesAllFields() throws Exception {
        JsonObject obj = new JsonObject();
        obj.put("id", "u1");
        obj.put("username", "justin");
        obj.put("name", "Justin");
        obj.put("avatar_url", "https://example.com/a.png");
        obj.put("bio", "hello");
        obj.put("created", "2024-01-01T00:00:00Z");
        obj.put("role", "admin");

        User user = parseUser(obj);
        assertEquals("u1", user.getId());
        assertEquals("justin", user.getUsername());
        assertEquals("Justin", user.getName());
        assertEquals("https://example.com/a.png", user.getAvatarUrl());
        assertEquals("hello", user.getBio());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), user.getCreated());
        assertEquals("admin", user.getRole());
    }

    @Test
    void parseUser_leavesMissingFieldsNull() throws Exception {
        User user = parseUser(new JsonObject());
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getName());
        assertNull(user.getAvatarUrl());
        assertNull(user.getBio());
        assertNull(user.getCreated());
        assertNull(user.getRole());
    }

    @Test
    void parseUser_returnsNull_whenObjectNull() throws Exception {
        assertNull(parseUser(null));
    }

    @Test
    void toJsonArray_buildsArray() throws Exception {
        assertEquals("[\"a\",\"b\"]", toJsonArray("a", "b"));
    }

    @Test
    void toJsonArray_returnsEmptyArrayForNoValues() throws Exception {
        assertEquals("[]", toJsonArray());
    }

    @Test
    void toJsonArray_skipsNullOrBlank() throws Exception {
        assertEquals("[\"a\"]", toJsonArray("a", null, ""));
    }

    @Test
    void toJsonArray_escapesQuotesAndBackslashes() throws Exception {
        assertEquals("[\"a\\\"b\",\"c\\\\d\"]", toJsonArray("a\"b", "c\\d"));
    }
}