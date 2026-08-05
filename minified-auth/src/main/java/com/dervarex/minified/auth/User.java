package com.dervarex.minified.auth;

import com.google.gson.JsonObject;

/**
 * Represents a logged-in user
 * {@link #uuid} is the unique identifier for the user (Mojang UUID)
 * {@link #username} is the display name of the user
 * {@link #accessToken} is the token used for authenticated requests
 * {@link #serializedSession} is the full session data as returned by the authentication server
 */
public record User(String uuid, String username, String accessToken, JsonObject serializedSession) { }