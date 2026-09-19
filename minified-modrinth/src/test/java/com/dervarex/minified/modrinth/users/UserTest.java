package com.dervarex.minified.modrinth.users;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void getters_returnSetValues() {
        User user = new User();
        user.id = "u1";
        user.username = "justin";
        user.name = "Justin";
        user.avatarUrl = "https://example.com/a.png";
        user.bio = "hello";
        user.created = Instant.parse("2024-01-01T00:00:00Z");
        user.role = "admin";

        assertEquals("u1", user.getId());
        assertEquals("justin", user.getUsername());
        assertEquals("Justin", user.getName());
        assertEquals("https://example.com/a.png", user.getAvatarUrl());
        assertEquals("hello", user.getBio());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), user.getCreated());
        assertEquals("admin", user.getRole());
    }
}