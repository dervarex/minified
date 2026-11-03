package com.dervarex.minified.modrinth.users;

import com.dervarex.minified.modrinth.internal.ModrinthJson;
import com.dervarex.minified.utils.json.JsonObject;
import org.apiguardian.api.API;

import java.time.Instant;

/**
 * A Modrinth user
 */
public final class User {
    private final String id;
    private final String username;
    private final String avatarUrl;
    private final String bio;
    private final Instant created;
    private final String role;

    /**
     * Reads a user from Modrinth's JSON, the clients do this.
     *
     * @param json the user as Modrinth sends it
     */
    @API(status = API.Status.INTERNAL)
    public User(JsonObject json) {
        id = ModrinthJson.string(json, "id");
        username = ModrinthJson.string(json, "username");
        avatarUrl = ModrinthJson.string(json, "avatar_url");
        bio = ModrinthJson.string(json, "bio");
        created = ModrinthJson.instant(json, "created");
        role = ModrinthJson.string(json, "role");
    }

    /** {@return the ID of the user} */
    public String getId() {
        return id;
    }

    /** {@return the username} */
    public String getUsername() {
        return username;
    }

    /** {@return the URL of the user's avatar} */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /** {@return the bio, {@code null} if the user didn't write one} */
    public String getBio() {
        return bio;
    }

    /** {@return when the account was created} */
    public Instant getCreated() {
        return created;
    }

    /** {@return the role on Modrinth, like {@code developer}, {@code moderator} or {@code admin}} */
    public String getRole() {
        return role;
    }

    /**
     * Describes the user, for logging purposes
     *
     * @return the username and ID
     */
    @Override
    public String toString() {
        return "User{" + username + ", " + id + "}";
    }
}
