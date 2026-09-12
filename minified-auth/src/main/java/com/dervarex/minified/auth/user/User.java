package com.dervarex.minified.auth.user;

import com.google.gson.JsonObject;
import org.apiguardian.api.API;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a logged-in user
 * {@link #uuid} is the unique identifier for the user (Mojang UUID)
 * {@link #username} is the display name of the user
 * {@link #accessToken} is the token used for authenticated requests
 * {@link #serializedSession} is the full session data as returned by the authentication server
 */
@API(status = API.Status.STABLE)
public final class User implements MinecraftAccount {
    private final MinecraftUUID uuid;
    private final String username;
    private final String accessToken;
    private final JsonObject serializedSession;

    public User(UUID uuid, String username, String accessToken, JsonObject serializedSession) {
        this.uuid = new MinecraftUUID(uuid);
        this.username = username;
        this.accessToken = accessToken;
        this.serializedSession = serializedSession;
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

    public String accessToken() {
        return accessToken;
    }

    public JsonObject serializedSession() {
        return serializedSession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User other = (User) o;
        return Objects.equals(uuid, other.uuid)
                && Objects.equals(username, other.username)
                && Objects.equals(accessToken, other.accessToken)
                && Objects.equals(serializedSession, other.serializedSession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, username, accessToken, serializedSession);
    }

    @Override
    public String toString() {
        return "User[" +
                "uuid=" + uuid + ", " +
                "username=" + username + ", " +
                "accessToken=" + accessToken + ", " +
                "serializedSession=" + serializedSession + ']';
    }
}