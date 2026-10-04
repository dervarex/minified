package com.dervarex.minified.utils.version;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Keeps GET responses around for a few minutes, the manifest and version JSONs don't change that often
 */
final class RecentResponses {
    private static final long KEEP_NANOS = TimeUnit.MINUTES.toNanos(5);

    private final Map<String, Response> responses = new ConcurrentHashMap<>();

    String get(String url) throws HttpException, IOException {
        Response cached = responses.get(url);
        if (cached != null && System.nanoTime() - cached.fetchedAt() < KEEP_NANOS) {
            return cached.body();
        }
        String body = HttpUtil.get(url);
        responses.put(url, new Response(body, System.nanoTime()));
        return body;
    }

    private record Response(String body, long fetchedAt) {
    }
}
