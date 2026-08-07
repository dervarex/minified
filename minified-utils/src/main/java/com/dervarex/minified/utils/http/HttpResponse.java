package com.dervarex.minified.utils.http;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record HttpResponse(int statusCode, String statusMessage, Map<String, String> headers, byte[] body) {
    public HttpResponse(int statusCode, String statusMessage, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage == null ? "" : statusMessage;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.body = body == null ? new byte[0] : body.clone();
    }

    @Override
    public byte[] body() {
        return body.clone();
    }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }
}

