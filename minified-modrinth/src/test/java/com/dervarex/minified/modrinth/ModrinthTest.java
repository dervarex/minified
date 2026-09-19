package com.dervarex.minified.modrinth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModrinthTest {

    @Test
    void defaultConstructor_usesDefaultBaseUrl() {
        Modrinth modrinth = new Modrinth();
        assertEquals(Modrinth.DEFAULT_BASE_URL, modrinth.getBaseUrl());
    }

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
    void constructor_trimsBaseUrl() {
        Modrinth modrinth = new Modrinth("  http://test  ");
        assertEquals("http://test", modrinth.getBaseUrl());
    }

    @Test
    void constructor_removesTrailingSlashes() {
        Modrinth modrinth = new Modrinth("http://test///");
        assertEquals("http://test", modrinth.getBaseUrl());
    }

    @Test
    void constructor_throws_whenBaseUrlNull() {
        assertThrows(NullPointerException.class, () -> new Modrinth(null));
    }

    @Test
    void constructor_throws_whenBaseUrlBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Modrinth("   "));
    }

    @Test
    void projects_returnsClient() {
        Modrinth modrinth = new Modrinth("http://test");
        assertNotNull(modrinth.projects());
    }

    @Test
    void versions_returnsClient() {
        Modrinth modrinth = new Modrinth("http://test");
        assertNotNull(modrinth.versions());
    }

    @Test
    void tags_returnsClient() {
        Modrinth modrinth = new Modrinth("http://test");
        assertNotNull(modrinth.tags());
    }

    @Test
    void users_returnsClient() {
        Modrinth modrinth = new Modrinth("http://test");
        assertNotNull(modrinth.users());
    }

    @Test
    void teams_returnsClient() {
        Modrinth modrinth = new Modrinth("http://test");
        assertNotNull(modrinth.teams());
    }
}