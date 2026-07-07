package com.dervarex.minified.modrinth.users;

import java.time.Instant;

public class User {
    public String id;
    public String username;
    public String name;
    public String avatarUrl;
    public String bio;
    public Instant created;
    public String role;

    public User() {
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public Instant getCreated() {
        return created;
    }

    public String getRole() {
        return role;
    }
}

