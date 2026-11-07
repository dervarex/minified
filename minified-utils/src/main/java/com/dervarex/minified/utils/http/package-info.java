/**
 * HTTP request and response utilities.
 * <br>
 * {@link com.dervarex.minified.utils.http.HttpUtil} is a small wrapper around {@link java.net.HttpURLConnection}, most
 * network calls in minified go through it
 *
 * <h2>Requests</h2>
 * A plain GET:
 * <pre>{@code
 * String body = HttpUtil.get("https://example.com/data.json");
 * }</pre>
 * For anything else, use {@link com.dervarex.minified.utils.http.HttpUtil#request request}:
 * <pre>{@code
 * HttpResponse response = HttpUtil.request(
 *         "POST",
 *         "https://example.com/api",
 *         Map.of("Authorization", "Bearer " + token),
 *         payloadBytes,
 *         10_000, // connect timeout ms
 *         10_000  // read timeout ms
 * );
 * String text = response.getBodyAsString();
 * }</pre>
 * {@link com.dervarex.minified.utils.http.HttpUtil#requestJson requestJson} sets the JSON headers and encodes the body as UTF-8
 *
 * <h2>Errors</h2>
 * Every status that is not 2xx throws a {@link com.dervarex.minified.utils.exceptions.HttpException}, you will not get
 * {@link com.dervarex.minified.utils.http.HttpResponse} with an error status <br>
 * 429, 502, 503 and 504 count as {@link com.dervarex.minified.utils.exceptions.HttpException#isTransientFailure() transient}
 */
package com.dervarex.minified.utils.http;
