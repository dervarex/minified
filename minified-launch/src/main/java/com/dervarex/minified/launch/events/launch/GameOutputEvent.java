package com.dervarex.minified.launch.events.launch;

import com.dervarex.minified.events.Event;

/**
 * Gets fired for every line the game prints, only when {@code captureGameOutput} is enabled in the launch configuration
 *
 * @param line   the line, without the line break
 * @param stream which output stream the line came from
 */
public record GameOutputEvent(String line, Stream stream) implements Event {

    public enum Stream {
        STDOUT,
        STDERR
    }
}
