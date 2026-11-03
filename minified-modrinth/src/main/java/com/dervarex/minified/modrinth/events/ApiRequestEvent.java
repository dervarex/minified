package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

import java.time.Duration;

/**
 * Posted after every request to the Modrinth API. Good for logging, debugging
 * or a request counter. File downloads aren't API requests, use the {@link DownloadProgressEvent} and similar instead.
 *
 * @param method     the HTTP method, like {@code GET} or {@code POST}
 * @param url        the full request URL
 * @param statusCode the HTTP status code, or <b>-1</b> if Modrinth couldn't be reached at all
 * @param duration   how long the request took
 */
@API(status = API.Status.EXPERIMENTAL)
public record ApiRequestEvent(String method, String url, int statusCode, Duration duration) implements Event {

    /**
     * {@return true for 2xx status codes}
     */
    public boolean successful() {
        return statusCode >= 200 && statusCode < 300;
    }
}
