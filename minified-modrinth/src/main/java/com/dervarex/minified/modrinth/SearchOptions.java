package com.dervarex.minified.modrinth;

import org.apiguardian.api.API;

/**
 * Common pagination options for Modrinth list and search endpoints.
 */
@API(status = API.Status.STABLE)
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

