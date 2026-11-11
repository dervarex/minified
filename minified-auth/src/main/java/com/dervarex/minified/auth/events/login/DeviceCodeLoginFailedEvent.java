package com.dervarex.minified.auth.events.login;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

/**
 * Gets fired when a login started with {@code AuthManager.startDeviceCodeLoginAsync()} failed. The login state
 * changes to {@code ERROR} with a short message, {@link cause} is the full error
 *
 * @param cause why it failed
 */
@API(status = API.Status.STABLE)
public record DeviceCodeLoginFailedEvent(Exception cause) implements Event {
}
