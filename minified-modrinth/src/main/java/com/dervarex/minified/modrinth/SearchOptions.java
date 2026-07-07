package com.dervarex.minified.modrinth;

/**
 * Common pagination options for Modrinth list and search endpoints.
 */
public class SearchOptions {
    public int limit = 10;
    public int offset = 0;

    public SearchOptions() {
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }
}

