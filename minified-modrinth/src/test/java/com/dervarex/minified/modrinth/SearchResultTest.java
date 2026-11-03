package com.dervarex.minified.modrinth;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchResultTest {

    @Test
    void constructor_setsFields() {
        SearchResult<String> result = new SearchResult<>(List.of("a", "b"), 10, 20, 100);
        assertEquals(List.of("a", "b"), result.getHits());
        assertEquals(10, result.getOffset());
        assertEquals(20, result.getLimit());
        assertEquals(100, result.getTotalHits());
    }

    @Test
    void hits_cantBeChanged() {
        List<String> hits = new ArrayList<>(List.of("a"));
        SearchResult<String> result = new SearchResult<>(hits, 0, 0, 1);
        hits.add("b");
        assertEquals(List.of("a"), result.getHits());
        assertThrows(UnsupportedOperationException.class, () -> result.getHits().add("c"));
    }

    @Test
    void isEmpty_returnsTrue_whenHitsEmpty() {
        assertTrue(new SearchResult<String>(List.of(), 0, 0, 0).isEmpty());
    }

    @Test
    void isEmpty_returnsFalse_whenHitsPresent() {
        assertFalse(new SearchResult<>(List.of("a"), 0, 0, 1).isEmpty());
    }

    @Test
    void toString_includesFields() {
        String text = new SearchResult<>(List.of("a"), 1, 2, 3).toString();
        assertTrue(text.contains("offset=1"));
        assertTrue(text.contains("limit=2"));
        assertTrue(text.contains("totalHits=3"));
    }
}
