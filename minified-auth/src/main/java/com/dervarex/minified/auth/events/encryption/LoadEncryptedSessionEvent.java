package com.dervarex.minified.auth.events.encryption;

import com.dervarex.minified.events.Event;

import java.nio.file.Path;
import org.apiguardian.api.API;

/**
 * Gets fired right after {@code AuthManager} successfully decrypts a previously saved session from disk
 *
 * @param SessionFile the file the encrypted session was loaded from
 */
@API(status = API.Status.STABLE)
public record LoadEncryptedSessionEvent(Path SessionFile) implements Event {
}
