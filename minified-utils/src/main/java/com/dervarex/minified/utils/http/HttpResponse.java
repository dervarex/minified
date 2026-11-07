package com.dervarex.minified.utils.http;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.apiguardian.api.API;

/**
 * A response from {@link HttpUtil}
 *
 * @param statusCode the status code
 * @param statusMessage the status message
 * @param headers the headers, the names ignore case and headers with more than one value are joined with ,
 * @param body the body
 */
@API(status = API.Status.STABLE)
public record HttpResponse(int statusCode, String statusMessage, Map<String, String> headers, byte[] body) {
    /**
     * @param statusCode the status code
     * @param statusMessage the status message, null becomes empty
     * @param headers the headers, null becomes empty
     * @param body the body, copied, null becomes empty
     */
    public HttpResponse(int statusCode, String statusMessage, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage == null ? "" : statusMessage;
        this.headers = caseInsensitive(headers);
        this.body = body == null ? new byte[0] : body.clone();
    }

    /**
     * {@return a copy of the body}
     */
    @Override
    public byte[] body() {
        return body.clone();
    }

    /**
     * {@return the body as UTF-8 text}
     */
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
