package com.dervarex.minified.auth.user;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OfflineUserTest {

    @Test
    void constructor_setsUsername() {
        OfflineUser user = new OfflineUser("Notch");
        assertEquals("Notch", user.username());
    }

    @Test
    void constructor_generatesOfflineUuid() {
        OfflineUser user = new OfflineUser("Notch");

        UUID expected = UUID.nameUUIDFromBytes(
                ("OfflinePlayer:Notch").getBytes(StandardCharsets.UTF_8));

        assertNotNull(user.getMinecraftUUID());
        assertEquals(expected, user.getMinecraftUUID().getDashed());
    }

    @Test
    void constructor_sameUsername_sameUuid() {
        OfflineUser a = new OfflineUser("Notch");
        OfflineUser b = new OfflineUser("Notch");

        assertEquals(a.getMinecraftUUID().getDashed(),
                b.getMinecraftUUID().getDashed());
    }

    @Test
    void constructor_differentUsername_differentUuid() {
        OfflineUser a = new OfflineUser("Notch");
        OfflineUser b = new OfflineUser("Herobrine");

        assertNotEquals(a.getMinecraftUUID().getDashed(),
                b.getMinecraftUUID().getDashed());
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        OfflineUser user = new OfflineUser("Notch");
        assertTrue(user.equals(user));
    }

    @Test
    void equals_sameUsername_returnsTrue() {
        OfflineUser a = new OfflineUser("Notch");
        OfflineUser b = new OfflineUser("Notch");
        assertEquals(a, b);
    }

    @Test
    void equals_differentUsername_returnsFalse() {
        OfflineUser a = new OfflineUser("Notch");
        OfflineUser b = new OfflineUser("Herobrine");
        assertNotEquals(a, b);
    }

    @Test
    void equals_null_returnsFalse() {
        assertNotEquals(null, new OfflineUser("Notch"));
    }

    @Test
    void equals_differentType_returnsFalse() {
        assertNotEquals("Notch", new OfflineUser("Notch"));
    }

    @Test
    void hashCode_sameUsername_equal() {
        assertEquals(new OfflineUser("Notch").hashCode(),
                new OfflineUser("Notch").hashCode());
    }

    @Test
    void toString_containsUsernameAndUuid() {
        String s = new OfflineUser("Notch").toString();
        assertTrue(s.contains("Notch"));
        assertTrue(s.contains("uuid="));
    }
}