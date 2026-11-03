package com.dervarex.minified.modrinth;

import org.apiguardian.api.API;

import java.util.List;

/**
 * One page of search results
 *
 * @param <T> what was searched for
 */
@API(status = API.Status.STABLE)
public final class SearchResult<T> {
    private final List<T> hits;
    private final int offset;
    private final int limit;
    private final int totalHits;

    @API(status = API.Status.INTERNAL)
    public SearchResult(List<T> hits, int offset, int limit, int totalHits) {
        this.hits = List.copyOf(hits);
        this.offset = offset;
        this.limit = limit;
        this.totalHits = totalHits;
    }

    /** {@return the results on this page} */
    public List<T> getHits() {
        return hits;
    }

    /** {@return how many results were skipped before this page} */
    public int getOffset() {
        return offset;
    }

    /** {@return how many results a page has at most} */
    public int getLimit() {
        return limit;
    }

    /** {@return how many results there are on all pages together} */
    public int getTotalHits() {
        return totalHits;
    }

    /** {@return whether this page has no results} */
    public boolean isEmpty() {
        return hits.isEmpty();
    }

    @Override
    public String toString() {
        return "SearchResult{hits=" + hits + ", offset=" + offset + ", limit=" + limit + ", totalHits=" + totalHits + '}';
    }
}
