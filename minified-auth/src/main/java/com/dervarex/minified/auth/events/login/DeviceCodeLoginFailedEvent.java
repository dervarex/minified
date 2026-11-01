package com.dervarex.minified.auth.events.login;

import com.dervarex.minified.events.Event;

/**
 * Gets fired when a login started with {@code AuthManager.startDeviceCodeLoginAsync()} failed. The login state
 * changes to {@code ERROR} with a short message, {@link cause} is the full error
 *
 * @param cause why it failed
 */
public record DeviceCodeLoginFailedEvent(Exception cause) implements Event {
}
