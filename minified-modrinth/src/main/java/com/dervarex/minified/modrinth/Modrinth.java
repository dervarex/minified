package com.dervarex.minified.modrinth;

import com.dervarex.minified.modrinth.projects.ProjectsClient;
import com.dervarex.minified.modrinth.tags.TagsClient;
import com.dervarex.minified.modrinth.teams.TeamsClient;
import com.dervarex.minified.modrinth.users.UsersClient;
import com.dervarex.minified.modrinth.versions.VersionsClient;
import org.apiguardian.api.API;

import java.util.Objects;

/**
 * Entry point for the public Modrinth API.
 */
@API(status = API.Status.STABLE)
// This class could be a record class, but we've got a few constructor problems here when converting it
@SuppressWarnings("ClassCanBeRecord")
public final class Modrinth {
    public static final String DEFAULT_BASE_URL = "https://api.modrinth.com/v2";

    private final String baseUrl;
    private final ProjectsClient projects;
    private final VersionsClient versions;
    private final TagsClient tags;
    private final UsersClient users;
    private final TeamsClient teams;

    public Modrinth() {
        this(DEFAULT_BASE_URL);
    }

    public Modrinth(String baseUrl) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.projects = new ProjectsClient(this);
        this.versions = new VersionsClient(this);
        this.tags = new TagsClient(this);
        this.users = new UsersClient(this);
        this.teams = new TeamsClient(this);
    }

    public static Modrinth connect() {
        return new Modrinth();
    }

    public static Modrinth connect(String baseUrl) {
        return new Modrinth(baseUrl);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public ProjectsClient projects() {
        return projects;
    }

    public VersionsClient versions() {
        return versions;
    }

    public TagsClient tags() {
        return tags;
    }

    public UsersClient users() {
        return users;
    }

    public TeamsClient teams() {
        return teams;
    }

    private static String normalizeBaseUrl(String baseUrl) {
        String value = Objects.requireNonNull(baseUrl, "baseUrl").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}

