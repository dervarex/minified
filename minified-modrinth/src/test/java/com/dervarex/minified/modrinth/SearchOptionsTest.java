package com.dervarex.minified.modrinth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchOptionsTest {

    @Test
    void defaultLimit_isTen() {
        assertEquals(10, new SearchOptions().getLimit());
    }

    @Test
    void defaultOffset_isZero() {
        assertEquals(0, new SearchOptions().getOffset());
    }

    @Test
    void getLimit_returnsSetValue() {
        SearchOptions options = new SearchOptions();
        options.limit = 25;
        assertEquals(25, options.getLimit());
    }

    @Test
    void getOffset_returnsSetValue() {
        SearchOptions options = new SearchOptions();
        options.offset = 50;
        assertEquals(50, options.getOffset());
    }
}