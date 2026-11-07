package com.dervarex.minified.utils.http;

import com.dervarex.minified.utils.exceptions.HttpException;
import org.apiguardian.api.API;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple yet advanced Http Utility
 */
@API(status = API.Status.STABLE)
public final class HttpUtil {
    private static final int DEFAULT_TIMEOUT_MS = 10_000;

    private HttpUtil() {}

    /**
     * GET with a 10 second timeout
     *
     * @param url the url
     * @throws IOException if the connection fails
     * @throws HttpException if the status isn't 2xx
     * @return the body as UTF-8 text
     */
    public static String get(String url) throws IOException, HttpException {
        return get(url, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Like above, but with your own timeout
     *
     * @param url the url
     * @param timeoutMs the connect and read timeout in ms
     * @throws IOException if the connection fails
     * @throws HttpException if the status isn't 2xx
     * @return the body as UTF-8 text
     */
    public static String get(String url, int timeoutMs) throws IOException, HttpException {
        HttpResponse response = request("GET", url, Map.of(), null, timeoutMs, timeoutMs);
        return response.getBodyAsString();
    }

    /**
     * Sends any request
     *
     * @param method the method, like GET or POST
     * @param url the url
     * @param headers the request headers, null for none
     * @param body the request body, null or empty for none
     * @param connectTimeoutMs the connect timeout in ms
     * @param readTimeoutMs the read timeout in ms
     * @throws IOException if the connection fails
     * @throws HttpException if the status isn't 2xx
     * @return the response
     */
    public static HttpResponse request(String method,
                                       String url,
                                       Map<String, String> headers,
                                       byte[] body,
                                       int connectTimeoutMs,
                                       int readTimeoutMs) throws IOException, HttpException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout(readTimeoutMs);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (body != null && body.length > 0) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Length", String.valueOf(body.length));
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body);
            }
        }

        int status = connection.getResponseCode();
        String message = connection.getResponseMessage();
        Map<String, String> responseHeaders = flattenHeaders(connection.getHeaderFields());

        byte[] responseBody = readBody(status < 400 ? connection.getInputStream() : connection.getErrorStream());
        HttpResponse response = new HttpResponse(status, message, responseHeaders, responseBody);

        if (status < 200 || status >= 300) {
            throw buildHttpException(method, url, response);
        }
        return response;
    }

    /**
     * Sends JSON, with Content-Type and Accept set to application/json and a 10 second timeout
     *
     * @param method the method, like POST
     * @param url the url
     * @param jsonBody the JSON, null for none
     * @throws IOException if the connection fails
     * @throws HttpException if the status isn't 2xx
     * @return the response
     */
    public static HttpResponse requestJson(String method, String url, String jsonBody) throws IOException, HttpException {
        byte[] body = jsonBody == null ? new byte[0] : jsonBody.getBytes(StandardCharsets.UTF_8);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return request(method, url, headers, body, DEFAULT_TIMEOUT_MS, DEFAULT_TIMEOUT_MS);
    }

    private static byte[] readBody(InputStream stream) throws IOException {
        if (stream == null) return new byte[0];
        try (InputStream input = stream) {
            return input.readAllBytes();
        }
    }

    private static Map<String, String> flattenHeaders(Map<String, List<String>> headerFields) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (headerFields == null) return headers;
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            String key = entry.getKey();
            if (key == null) continue;
            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) continue;
            headers.put(key, String.join(",", values));
        }
        return headers;
    }

    private static HttpException buildHttpException(String method, String url, HttpResponse response) {
        HttpException.Method httpMethod = HttpException.Method.valueOf(method.toUpperCase());
        int code = response.statusCode();
        boolean transientFailure = code == 429 || code == 502 || code == 503 || code == 504;
        String requestId = response.headers().getOrDefault("X-Request-Id", "");
        return new HttpException.Builder()
                .status(code)
                .statusMessage(response.statusMessage())
                .method(httpMethod)
                .url(url)
                .headers(response.headers())
                .body(response.body())
                .requestId(requestId)
                .transientFailure(transientFailure)
                .build();
    }
}

