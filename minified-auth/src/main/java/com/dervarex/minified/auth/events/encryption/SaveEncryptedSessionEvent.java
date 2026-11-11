package com.dervarex.minified.auth.events.encryption;

import com.dervarex.minified.events.Event;

import java.nio.file.Path;
import org.apiguardian.api.API;

/**
 * Gets fired right after {@code AuthManager} finishes encrypting and saving a session to disk
 *
 * @param SessionFile the file the encrypted session was written to
 */
@API(status = API.Status.STABLE)
public record SaveEncryptedSessionEvent(Path SessionFile) implements Event {
}
