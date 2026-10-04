package com.dervarex.minified.utils.http;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public record HttpResponse(int statusCode, String statusMessage, Map<String, String> headers, byte[] body) {
    public HttpResponse(int statusCode, String statusMessage, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage == null ? "" : statusMessage;
        this.headers = caseInsensitive(headers);
        this.body = body == null ? new byte[0] : body.clone();
    }

    @Override
    public byte[] body() {
        return body.clone();
    }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    // http headers don't care about case, so neither do we
    static Map<String, String> caseInsensitive(Map<String, String> headers) {
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (headers != null) {
            headers.forEach((key, value) -> {
                if (key != null) result.put(key, value);
            });
        }
        return Collections.unmodifiableMap(result);
    }
}
