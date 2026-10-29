package com.dervarex.minified.events.type.connection;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

/**
 * Gets fired when we're checking whether the user is online or not
 */
@API(status = API.Status.STABLE)
public record CheckConnectionEvent() implements Event {
}