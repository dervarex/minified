package com.dervarex.minified.auth.user;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final UUID UUID_A =
            UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
    private static final UUID UUID_B =
            UUID.fromString("11111111-2222-3333-4444-555555555555");

    private static JsonObject session(String token) {
        JsonObject obj = new JsonObject();
        obj.addProperty("accessToken", token);
        return obj;
    }

    private static User createUser(UUID uuid, String name, String token) {
        return new User(uuid, name, token, session(token));
    }

    @Test
    void getters_returnConstructorValues() {
        User user = createUser(UUID_A, "Notch", "token123");

        assertEquals("Notch", user.username());
        assertEquals("token123", user.accessToken());
        assertNotNull(user.getMinecraftUUID());
        assertNotNull(user.serializedSession());
    }

    @Test
    void getMinecraftUUID_containsDashedAndUndashed() {
        User user = createUser(UUID_A, "Notch", "token123");

        assertEquals(UUID_A, user.getMinecraftUUID().getDashed());
        assertEquals("069a79f444e94726a5befca90e38aaf5",
                user.getMinecraftUUID().getUndashed());
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        User user = createUser(UUID_A, "Notch", "token123");
        assertTrue(user.equals(user));
    }

    @Test
    void equals_allFieldsEqual_returnsTrue() {
        User a = createUser(UUID_A, "Notch", "token123");
        User b = createUser(UUID_A, "Notch", "token123");
        assertEquals(a, b);
    }

    @Test
    void equals_differentUuid_returnsFalse() {
        User a = createUser(UUID_A, "Notch", "token123");
        User b = createUser(UUID_B, "Notch", "token123");
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentUsername_returnsFalse() {
        User a = createUser(UUID_A, "Notch", "token123");
        User b = createUser(UUID_A, "Herobrine", "token123");
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentAccessToken_returnsFalse() {
        User a = createUser(UUID_A, "Notch", "token123");
        User b = createUser(UUID_A, "Notch", "token456");
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentSerializedSession_returnsFalse() {
        User a = new User(UUID_A, "Notch", "token123", session("x"));
        User b = new User(UUID_A, "Notch", "token123", session("y"));
        assertNotEquals(a, b);
    }

    @Test
    void equals_null_returnsFalse() {
        assertNotEquals(null, createUser(UUID_A, "Notch", "token123"));
    }

    @Test
    void equals_differentType_returnsFalse() {
        assertNotEquals("Notch", createUser(UUID_A, "Notch", "token123"));
    }

    @Test
    void hashCode_equalObjects_sameHashCode() {
        User a = createUser(UUID_A, "Notch", "token123");
        User b = createUser(UUID_A, "Notch", "token123");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_containsAllFields() {
        String s = createUser(UUID_A, "Notch", "token123").toString();
        assertTrue(s.contains("Notch"));
        assertTrue(s.contains("token123"));
        assertTrue(s.contains("uuid="));
        assertTrue(s.contains("serializedSession="));
    }
}