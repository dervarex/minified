package com.dervarex.minified.modrinth.internal;

import com.dervarex.minified.modrinth.Modrinth;
import com.dervarex.minified.modrinth.events.ApiRequestEvent;
import com.dervarex.minified.modrinth.events.RateLimitedEvent;
import com.dervarex.minified.modrinth.exceptions.ModrinthApiException;
import com.dervarex.minified.modrinth.exceptions.ModrinthNotFoundException;
import com.dervarex.minified.modrinth.exceptions.ModrinthRateLimitedException;
import com.dervarex.minified.modrinth.exceptions.ModrinthSerializationException;
import com.dervarex.minified.utils.exceptions.HttpException;
import com.dervarex.minified.utils.http.HttpResponse;
import com.dervarex.minified.utils.http.HttpUtil;
import com.dervarex.minified.utils.json.JsonArray;
import com.dervarex.minified.utils.json.JsonObject;
import com.dervarex.minified.utils.json.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Shared HTTP/JSON helper for Modrinth endpoint clients.
 */
public abstract class AbstractModrinthClient {
    private static final int TIMEOUT_MS = 10_000;

    protected final Modrinth modrinth;

    /**
     * Creates a client that talks to the API of a {@link Modrinth}
     *
     * @param modrinth the Modrinth that it belongs to
     */
    protected AbstractModrinthClient(Modrinth modrinth) {
        this.modrinth = Objects.requireNonNull(modrinth, "modrinth");
    }

    /**
     * Sends a GET request and reads the answer as a JSON object
     *
     * @param path the API path, like {@code /project/sodium}
     * @return the answer
     */
    protected JsonObject getObject(String path) {
        return getObject(path, Map.of());
    }

    /**
     * Sends a GET request and reads the answer as a JSON object
     *
     * @param path  the API path, like {@code /project/sodium}
     * @param query the query parameters
     * @return the answer
     */
    protected JsonObject getObject(String path, Map<String, String> query) {
        return readJsonObject(request(path, query));
    }

    /**
     * Sends a GET request and reads the answer as a JSON array.
     *
     * @param path the API path, like {@code /project/sodium}
     * @return the answer
     */
    protected JsonArray getArray(String path) {
        return getArray(path, Map.of());
    }

    /**
     * Sends a GET request and reads the answer as a JSON array.
     *
     * @param path  the API path, like {@code /project/sodium}
     * @param query the query parameters
     * @return the answer
     */
    protected JsonArray getArray(String path, Map<String, String> query) {
        return readJsonArray(request(path, query));
    }

    /**
     * Sends a GET request and turns the JSON object it answers with into a model.
     *
     * @param path   the API path, like {@code /project/sodium}
     * @param parser turns the JSON into the model
     * @param <T> the type of the model
     * @return the model
     */
    protected <T> T get(String path, Function<JsonObject, T> parser) {
        return get(path, Map.of(), parser);
    }

    /**
     * Sends a GET request and turns the JSON object it answers with into a model.
     *
     * @param path   the API path, like {@code /project/sodium}
     * @param query  the query parameters
     * @param parser turns the JSON into the model
     * @param <T> the type of the model
     * @return the model
     */
    protected <T> T get(String path, Map<String, String> query, Function<JsonObject, T> parser) {
        return ModrinthJson.parse(getObject(path, query), parser);
    }

    /**
     * Sends a GET request and turns every object of the JSON array it answers with into a model
     *
     * @param path   the API path, like {@code /project/sodium}
     * @param query  the query parameters
     * @param parser turns one JSON object into a model
     * @param <T> the type of the model
     * @return the models
     */
    protected <T> List<T> getList(String path, Map<String, String> query, Function<JsonObject, T> parser) {
        return toList(getArray(path, query), parser);
    }

    /**
     * Turns every object of a JSON array into a model.
     *
     * @param array  the JSON array
     * @param parser turns one JSON object into a model
     * @param <T> the type of the model
     * @return the models
     */
    protected static <T> List<T> toList(JsonArray array, Function<JsonObject, T> parser) {
        return array.values().stream()
                .map(value -> value == null || value.isNull() ? null : value.asObject())
                .filter(Objects::nonNull)
                .map(object -> ModrinthJson.parse(object, parser))
                .toList();
    }

    /**
     * Writes values as a JSON array of strings, for the {@code ids=} and {@code hashes} parameters
     *
     * @param values the values
     * @return the JSON array, like {@code ["a","b"]}
     */
    protected static String toJsonArray(Iterable<String> values) {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(value.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        return builder.append(']').toString();
    }

    /**
     * Writes values as a JSON array of strings, for the {@code ids=} and {@code hashes} parameters
     *
     * @param values the values, can be {@code null}
     * @return the JSON array, like {@code ["a","b"]}
     */
    protected static String toJsonArray(String... values) {
        return toJsonArray(values == null ? List.of() : java.util.Arrays.asList(values));
    }

    /**
     * Sends a GET request
     *
     * @param path  the API path, like {@code /project/sodium}
     * @param query the query parameters, {@code null} values are left out
     * @return the body of the answer
     */
    protected String request(String path, Map<String, String> query) {
        String url = buildUrl(path, query);
        return send("GET", url, () -> HttpUtil.request("GET", url, Map.of(), null, TIMEOUT_MS, TIMEOUT_MS));
    }

    /**
     * Sends a POST request with a JSON body and reads the answer as a JSON object
     *
     * @param path     the API path, like {@code /project/sodium}
     * @param jsonBody the JSON to send
     * @return the answer
     */
    protected JsonObject postObject(String path, String jsonBody) {
        String url = buildUrl(path, Map.of());
        return readJsonObject(send("POST", url, () -> HttpUtil.requestJson("POST", url, jsonBody)));
    }

    private String send(String method, String url, HttpCall call) {
        long start = System.nanoTime();
        int status = -1;
        try {
            HttpResponse response = call.execute();
            status = response.statusCode();
            return response.getBodyAsString();
        } catch (HttpException ex) {
            status = ex.getStatusCode();
            throw translateHttpException(url, ex);
        } catch (IOException ex) {
            throw new ModrinthApiException("Failed to contact Modrinth at " + url, -1, url, ex);
        } finally {
            modrinth.getEventBus().post(new ApiRequestEvent(method, url, status, Duration.ofNanos(System.nanoTime() - start)));
        }
    }

    @FunctionalInterface
    private interface HttpCall {
        HttpResponse execute() throws IOException, HttpException;
    }

    /**
     * Parses a JSON object
     *
     * @param json the JSON text
     * @return the object
     * @throws com.dervarex.minified.modrinth.exceptions.ModrinthSerializationException if it isn't a JSON object
     */
    protected JsonObject readJsonObject(String json) {
        try {
            return JsonParser.parse(json).asObject();
        } catch (RuntimeException ex) {
            throw new ModrinthSerializationException("Failed to parse Modrinth JSON object", ex);
        }
    }

    /**
     * Parses a JSON array
     *
     * @param json the JSON text
     * @return the array
     * @throws com.dervarex.minified.modrinth.exceptions.ModrinthSerializationException if it isn't a JSON array
     */
    protected JsonArray readJsonArray(String json) {
        try {
            return JsonParser.parse(json).asArray();
        } catch (RuntimeException ex) {
            throw new ModrinthSerializationException("Failed to parse Modrinth JSON array", ex);
        }
    }

    /**
     * Builds the URL of a request
     *
     * @param path  the API path, like {@code /project/sodium}
     * @param query the query parameters, {@code null} values are left out
     * @return the URL
     */
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

    /**
     * Builds query parameters from key value pairs
     *
     * @param entries key, value, key, value and so on
     * @return the query parameters
     * @throws IllegalArgumentException if there's an odd number of entries
     */
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

    /**
     * URL encodes a value
     *
     * @param value the value
     * @return the encoded value
     */
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
            modrinth.getEventBus().post(new RateLimitedEvent(url, retryAfter));
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

