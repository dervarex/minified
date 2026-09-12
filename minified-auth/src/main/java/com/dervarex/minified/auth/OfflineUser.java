package com.dervarex.minified.auth;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an offline-mode user
 * {@link #uuid} is the offline uuid for the user (MD5 hash of username, converted to a Version 3 UUID)
 * {@link #username} is the display name of the user
 */
public class OfflineUser {
    private final UUID uuid;
    private final String username;

    public OfflineUser(String username) {
        this.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        this.username = username;
    }

    /**
     * @return player's uuid without dashes, as used in mojang's api
     */
    public String uuid() {
        return uuid.toString().replace("-", "");
    }

    /**
     * @return full uuid with dashes
     */
    public UUID dasheduuid() {
        return uuid;
    }

    public String username() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfflineUser)) return false;
        OfflineUser other = (OfflineUser) o;
        return Objects.equals(uuid, other.uuid)
                && Objects.equals(username, other.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, username);
    }

    @Override
    public String toString() {
        return "User[" +
                "uuid=" + uuid + ", " +
                "username=" + username + ", ";
    }
}
