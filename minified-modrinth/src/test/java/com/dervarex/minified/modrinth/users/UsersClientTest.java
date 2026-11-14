package com.dervarex.minified.modrinth.users;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.exceptions.ModrinthNotFoundException;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsersClientTest {

    private final UsersClient users = Modrinth.connect().users();

    @Test
    void parsesEveryField() {
        User user = new User(JsonParser.parse("""
                {"id":"u1","username":"justin","avatar_url":"https://example.com/a.png","bio":"hello",
                 "created":"2024-01-01T00:00:00Z","role":"admin"}""").asObject());

        assertEquals("u1", user.getId());
        assertEquals("justin", user.getUsername());
        assertEquals("https://example.com/a.png", user.getAvatarUrl());
        assertEquals("hello", user.getBio());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), user.getCreated());
        assertEquals("admin", user.getRole());
    }

    @Test
    void leavesMissingFieldsNull() {
        User user = new User(new JsonObject());
        assertNull(user.getId());
        assertNull(user.getBio());
        assertNull(user.getCreated());
    }

    @Test
    @Tag("manual")
    void getsAUserByUsername() {
        assertEquals("TEZXhE2U", users.get("jellysquid3").getId());
    }

    @Test
    @Tag("manual")
    void getsManyUsersByIdOrUsername() {
        List<String> names = users.getMany("TEZXhE2U").stream().map(User::getUsername).toList();
        assertEquals(List.of("jellysquid3"), names);
        assertEquals(List.of(), users.getMany());
    }

    @Test
    @Tag("manual")
    void unknownUsersAreNotFound() {
        assertThrows(ModrinthNotFoundException.class, () -> users.get("no-such-user-" + System.nanoTime()));
    }
}
