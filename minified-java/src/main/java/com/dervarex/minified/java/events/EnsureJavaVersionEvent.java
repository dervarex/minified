package com.dervarex.minified.java.events;

import com.dervarex.minified.events.Event;

/**
 * Called when the Java version {@code requestedMajorVersion} must be ensured before the game can launch.
 * <p>
 * Not called if the requested version (or a higher compatible one) is already installed.
 *
 * @param requestedMajorVersion the major Java version required to launch the game
 */
public record EnsureJavaVersionEvent(int requestedMajorVersion) implements Event {
}
