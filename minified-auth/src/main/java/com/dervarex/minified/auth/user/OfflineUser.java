package com.dervarex.minified.auth.user;

import org.apiguardian.api.API;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an offline-mode user
 * {@link #uuid} is the offline uuid for the user (MD5 hash of username, converted to a Version 3 UUID)
 * {@link #username} is the display name of the user
 */
@API(status = API.Status.STABLE, since = "v3.2.0")
public final class OfflineUser implements MinecraftAccount {
    private final MinecraftUUID uuid;
    private final String username;

    public OfflineUser(String username) {
        this.uuid = new MinecraftUUID(UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)));
        this.username = username;
    }

    /**
     * @return {@link MinecraftUUID}, contains dashed and non dashed uuid
     */
    @Override
    public MinecraftUUID getMinecraftUUID() {
        return uuid;
    }

    @Override
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
        return "OfflineUser[" +
                "uuid=" + uuid + ", " +
                "username=" + username + ']';
    }
}
