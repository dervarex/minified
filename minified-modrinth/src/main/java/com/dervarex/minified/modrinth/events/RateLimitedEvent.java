package com.dervarex.minified.modrinth.events;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

/**
 * Posted when Modrinth answers a request with 429 (too many requests). The request also gets a
 * {@link com.dervarex.minified.modrinth.exceptions.ModrinthRateLimitedException}, this event just lets a launcher
 * show it somewhere central, like "Modrinth is rate limiting you, chill and maybe try again later".
 *
 * @param url               the request that got rate limited
 * @param retryAfterSeconds how long Modrinth wants us to wait, or <b>0</b> if it doesn't want us to know
 */
@API(status = API.Status.EXPERIMENTAL)
public record RateLimitedEvent(String url, long retryAfterSeconds) implements Event {
}
