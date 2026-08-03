package com.dervarex.minified.auth.events.encryption;

import com.dervarex.minified.events.Event;

import java.nio.file.Path;

public record LoadEncryptedSessionEvent(Path SessionFile) implements Event {
}
