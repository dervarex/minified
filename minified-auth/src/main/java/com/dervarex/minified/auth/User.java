package com.dervarex.minified.auth;

import com.google.gson.JsonObject;

/**
 * Represents a logged-in user
 * {@link #uuid} is the unique identifier for the user (Mojang UUID)
 * {@link #username} is the display name of the user
 * {@link #accessToken} is the token used for authenticated requests
 * {@link #serializedSession} is the full session data as returned by the authentication server
 */
public class User {
    private final String uuid;
    private final String username;
    private final String accessToken;
    private final JsonObject serializedSession;

    public User(String uuid, String username, String accessToken, JsonObject serializedSession) {
        this.uuid = uuid;
        this.username = username;
        this.accessToken = accessToken;
        this.serializedSession = serializedSession;
    }

    public String getUuid() { return uuid; }
    public String getUsername() { return username; }
    public String getAccessToken() { return accessToken; }
    public JsonObject getSerializedSession() { return serializedSession; }
    public String getXuid() { return serializedSession.get("xuid").getAsString(); }
}