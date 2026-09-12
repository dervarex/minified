package com.dervarex.minified.auth.user;

import com.google.gson.JsonObject;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a logged-in user
 * {@link #uuid} is the unique identifier for the user (Mojang UUID)
 * {@link #username} is the display name of the user
 * {@link #accessToken} is the token used for authenticated requests
 * {@link #serializedSession} is the full session data as returned by the authentication server
 */
public final class User {
    private final UUID uuid;
    private final String username;
    private final String accessToken;
    private final JsonObject serializedSession;

    public User(UUID uuid, String username, String accessToken, JsonObject serializedSession) {
        this.uuid = uuid;
        this.username = username;
        this.accessToken = accessToken;
        this.serializedSession = serializedSession;
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