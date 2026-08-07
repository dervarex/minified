package com.dervarex.minified.modrinth.internal;

import com.dervarex.minified.modrinth.*;
import com.dervarex.minified.modrinth.exceptions.ModrinthApiException;
import com.dervarex.minified.modrinth.exceptions.ModrinthNotFoundException;
import com.dervarex.minified.modrinth.exceptions.ModrinthRateLimitedException;
import com.dervarex.minified.modrinth.exceptions.ModrinthSerializationException;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Shared HTTP/JSON helper for Modrinth endpoint clients.
 */
public abstract class AbstractModrinthClient {
    protected final Modrinth modrinth;

    protected AbstractModrinthClient(Modrinth modrinth) {
        this.modrinth = Objects.requireNonNull(modrinth, "modrinth");
    }

    protected JsonObject getObject(String path) {
        return getObject(path, Map.of());
    }

    protected JsonObject getObject(String path, Map<String, String> query) {
        return readJsonObject(request(path, query));
    }

    protected JsonArray getArray(String path) {
        return getArray(path, Map.of());
    }

    protected JsonArray getArray(String path, Map<String, String> query) {
        return readJsonArray(request(path, query));
    }

    protected String getRaw(String path, Map<String, String> query) {
        return request(path, query);
    }

    protected <T> List<T> getList(String path, Map<String, String> query, Function<JsonObject, T> parser) {
        JsonArray array = getArray(path, query);
        return array.values().stream()
                .map(value -> value == null || value.isNull() ? null : value.asObject())
                .filter(Objects::nonNull)
                .map(parser)
                .toList();
    }

    protected String request(String path, Map<String, String> query) {
        String url = buildUrl(path, query);
        try {
            return HttpUtil.get(url);
        } catch (HttpException ex) {
            throw translateHttpException(url, ex);
        } catch (IOException ex) {
            throw new ModrinthApiException("Failed to contact Modrinth at " + url, -1, url, ex);
        }
    }

    protected JsonObject readJsonObject(String json) {
        try {
            return JsonParser.parse(json).asObject();
        } catch (RuntimeException ex) {
            throw new ModrinthSerializationException("Failed to parse Modrinth JSON object", ex);
        }
    }

    protected JsonArray readJsonArray(String json) {
        try {
            return JsonParser.parse(json).asArray();
        } catch (RuntimeException ex) {
            throw new ModrinthSerializationException("Failed to parse Modrinth JSON array", ex);
        }
    }

    protected String buildUrl(String path, Map<String, String> query) {
        String normalizedPath = path == null ? "" : path.trim();
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        StringBuilder builder = new StringBuilder(modrinth.getBaseUrl()).append(normalizedPath);
        if (query != null && !query.isEmpty()) {
            boolean first = true;
            for (Map.Entry<String, String> entry : query.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                builder.append(first ? '?' : '&');
                first = false;
                builder.append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
            }
        }
        return builder.toString();
    }

    protected Map<String, String> query(Object... entries) {
        if (entries == null || entries.length == 0) {
            return Map.of();
        }
        if (entries.length % 2 != 0) {
            throw new IllegalArgumentException("Query entries must contain an even number of arguments");
        }
        Map<String, String> query = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            String key = Objects.toString(entries[i], null);
            String value = Objects.toString(entries[i + 1], null);
            if (key != null && value != null) {
                query.put(key, value);
            }
        }
        return query;
    }

    protected static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private ModrinthApiException translateHttpException(String url, HttpException ex) {
        int status = ex.getStatusCode();
        String message = ex.getStatusMessage().isBlank() ? ex.getMessage() : ex.getStatusMessage();
        if (status == 404) {
            return new ModrinthNotFoundException(message, status, url, ex);
        }
        if (status == 429) {
            long retryAfter = parseLong(ex.getResponseHeaders().getOrDefault("Retry-After", ex.getResponseHeaders().getOrDefault("retry-after", "0")));
            return new ModrinthRateLimitedException(message, status, url, retryAfter, ex);
        }
        return new ModrinthApiException(message, status, url, ex);
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}

