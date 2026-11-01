package com.dervarex.minified.auth.events.login;

import com.dervarex.minified.events.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Gets fired when a saved session couldn't be loaded or refreshed. <br>
 * Usually the refresh token expired or was revoked and the user has to log in again
 *
 * @param uuid  the account that was asked for, null for {@code loginWithSavedSession()} without an account
 * @param cause why it failed
 */
public record SessionRefreshFailedEvent(@Nullable UUID uuid, Exception cause) implements Event {
}
