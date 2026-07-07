package com.dervarex.minified.modrinth.teams;

import com.dervarex.minified.modrinth.users.User;

import java.time.Instant;

public class TeamMember {
    public User user;
    public String role;
    public String[] permissions;
    public Instant joined;

    public TeamMember() {
    }

    public User getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public Instant getJoined() {
        return joined;
    }
}

