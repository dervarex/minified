package com.dervarex.minified.modrinth.teams;

import java.time.Instant;

public class Team {
    public String id;
    public String name;
    public String description;
    public String iconUrl;
    public String url;
    public String[] projects;
    public Instant created;
    public Instant updated;

    public Team() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getUrl() {
        return url;
    }

    public String[] getProjects() {
        return projects;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getUpdated() {
        return updated;
    }
}

