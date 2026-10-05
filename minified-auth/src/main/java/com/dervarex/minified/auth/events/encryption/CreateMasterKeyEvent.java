package com.dervarex.minified.auth.events.encryption;

import com.dervarex.minified.events.Event;

import java.nio.file.Path;

/**
 * Gets fired when {@code AuthManager} generates a brand new master key used to encrypt/decrypt the saved
 * session file
 *
 * @param keyFile the file the new master key will be written to
 */
public record CreateMasterKeyEvent(Path keyFile) implements Event {
}