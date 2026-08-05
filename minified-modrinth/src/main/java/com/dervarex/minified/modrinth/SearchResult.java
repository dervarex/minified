package com.dervarex.minified.modrinth;

import org.apiguardian.api.API;

import java.util.Arrays;

/**
 * Generic search result container used by Modrinth endpoints.
 */
@API(status = API.Status.STABLE)
public final class SearchResult<T> {
    public T[] hits;
    public int offset;
    public int limit;
    public int totalHits;

    public SearchResult() {
    }

    public SearchResult(T[] hits, int offset, int limit, int totalHits) {
        this.hits = hits;
        this.offset = offset;
        this.limit = limit;
        this.totalHits = totalHits;
    }

    public T[] getHits() {
        return hits;
    }

    public boolean isEmpty() {
        return hits == null || hits.length == 0;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "hits=" + Arrays.toString(hits) +
                ", offset=" + offset +
                ", limit=" + limit +
                ", totalHits=" + totalHits +
                '}';
    }
}

