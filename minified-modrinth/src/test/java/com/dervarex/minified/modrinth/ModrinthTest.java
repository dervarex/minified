package com.dervarex.minified.modrinth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModrinthTest {

    @Test
    void connect_usesDefaultBaseUrl() {
        Modrinth modrinth = Modrinth.connect();
        assertEquals(Modrinth.DEFAULT_BASE_URL, modrinth.getBaseUrl());
    }

    @Test
    void connect_withBaseUrl_usesGivenUrl() {
        Modrinth modrinth = Modrinth.connect("http://test");
        assertEquals("http://test", modrinth.getBaseUrl());
    }

    @Test
    void connect_trimsBaseUrl() {
        Modrinth modrinth = Modrinth.connect("  http://test  ");
        assertEquals("http://test", modrinth.getBaseUrl());
    }

    @Test
    void connect_removesTrailingSlashes() {
        Modrinth modrinth = Modrinth.connect("http://test///");
        assertEquals("http://test", modrinth.getBaseUrl());
    }

    @Test
    void connect_throws_whenBaseUrlNull() {
        assertThrows(NullPointerException.class, () -> Modrinth.connect((String) null));
    }

    @Test
    void connect_throws_whenBaseUrlBlank() {
        assertThrows(IllegalArgumentException.class, () -> Modrinth.connect("   "));
    }

    @Test
    void connect_throws_whenEventBusNull() {
        assertThrows(NullPointerException.class, () -> Modrinth.connect("http://test", null));
    }

    @Test
    void connect_usesGivenEventBus() {
        com.dervarex.minified.events.EventBus eventBus = new com.dervarex.minified.events.EventBus();
        assertSame(eventBus, Modrinth.connect("http://test", eventBus).getEventBus());
    }

    @Test
    void projects_returnsClient() {
        Modrinth modrinth = Modrinth.connect("http://test");
        assertNotNull(modrinth.projects());
    }

    @Test
    void versions_returnsClient() {
        Modrinth modrinth = Modrinth.connect("http://test");
        assertNotNull(modrinth.versions());
    }

    @Test
    void tags_returnsClient() {
        Modrinth modrinth = Modrinth.connect("http://test");
        assertNotNull(modrinth.tags());
    }

    @Test
    void users_returnsClient() {
        Modrinth modrinth = Modrinth.connect("http://test");
        assertNotNull(modrinth.users());
    }

    @Test
    void teams_returnsClient() {
        Modrinth modrinth = Modrinth.connect("http://test");
        assertNotNull(modrinth.teams());
    }
}