package com.dervarex.minified.modrinth;

import com.dervarex.minified.events.EventBus;
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
public final class Modrinth {
    /** The public Modrinth API */
    public static final String DEFAULT_BASE_URL = "https://api.modrinth.com/v2";

    private final String baseUrl;
    private final EventBus eventBus;
    private final ProjectsClient projects;
    private final VersionsClient versions;
    private final TagsClient tags;
    private final UsersClient users;
    private final TeamsClient teams;

    private Modrinth(String baseUrl, EventBus eventBus) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
        this.projects = new ProjectsClient(this);
        this.versions = new VersionsClient(this);
        this.tags = new TagsClient(this);
        this.users = new UsersClient(this);
        this.teams = new TeamsClient(this);
    }

    /**
     * {@return a client for the public Modrinth API at {@link #DEFAULT_BASE_URL}, with its own EventBus}
     */
    public static Modrinth connect() {
        return connect(DEFAULT_BASE_URL);
    }

    /**
     * Creates a client for a Modrinth API at another URL, like a staging instance or a compatible mirror, with its own
     * EventBus
     *
     * @param baseUrl the Modrinth API URL
     * @return the client
     */
    public static Modrinth connect(String baseUrl) {
        return connect(baseUrl, new EventBus());
    }

    /**
     * Creates a client for a Modrinth API that posts its events to an EventBus you already have.
     *
     * @param baseUrl  the Modrinth API URL, usually {@link #DEFAULT_BASE_URL}
     * @param eventBus the EventBus the client posts its events to
     * @return the client
     */
    @API(status = API.Status.STABLE)
    public static Modrinth connect(String baseUrl, EventBus eventBus) {
        return new Modrinth(baseUrl, eventBus);
    }

    /** {@return the URL of the API this client talks to} */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * {@return the EventBus this client posts its events to}
     */
    @API(status = API.Status.STABLE)
    public EventBus getEventBus() {
        return eventBus;
    }

    /** {@return the client for projects} */
    public ProjectsClient projects() {
        return projects;
    }

    /** {@return the client for versions} */
    public VersionsClient versions() {
        return versions;
    }

    /** {@return the client for tags, like categories and loaders} */
    public TagsClient tags() {
        return tags;
    }

    /** {@return the client for users} */
    public UsersClient users() {
        return users;
    }

    /** {@return the client for teams} */
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

