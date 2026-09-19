package com.dervarex.minified.modrinth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchResultTest {

    @Test
    void constructor_setsFields() {
        SearchResult<String> result = new SearchResult<>(new String[]{"a", "b"}, 10, 20, 100);
        assertArrayEquals(new String[]{"a", "b"}, result.getHits());
        assertEquals(10, result.offset);
        assertEquals(20, result.limit);
        assertEquals(100, result.totalHits);
    }

    @Test
    void isEmpty_returnsTrue_whenHitsNull() {
        SearchResult<String> result = new SearchResult<>();
        assertTrue(result.isEmpty());
    }

    @Test
    void isEmpty_returnsTrue_whenHitsEmpty() {
        SearchResult<String> result = new SearchResult<>(new String[0], 0, 0, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    void isEmpty_returnsFalse_whenHitsPresent() {
        SearchResult<String> result = new SearchResult<>(new String[]{"a"}, 0, 0, 1);
        assertFalse(result.isEmpty());
    }

    @Test
    void toString_includesFields() {
        SearchResult<String> result = new SearchResult<>(new String[]{"a"}, 1, 2, 3);
        String text = result.toString();
        assertTrue(text.contains("offset=1"));
        assertTrue(text.contains("limit=2"));
        assertTrue(text.contains("totalHits=3"));
    }
}