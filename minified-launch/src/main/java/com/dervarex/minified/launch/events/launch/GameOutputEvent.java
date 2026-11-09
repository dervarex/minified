package com.dervarex.minified.launch.events.launch;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

/**
 * Gets fired for every line the game prints, only when {@code captureGameOutput} or {@code headless} is enabled in the
 * launch configuration. In headless mode stderr is merged into stdout
 *
 * @param line   the line, without the line break
 * @param stream which output stream the line came from
 */
@API(status = API.Status.STABLE)
public record GameOutputEvent(String line, Stream stream) implements Event {

    /**
     * The output stream a line came from
     */
    public enum Stream {
        /** Standard output */
        STDOUT,
        /** Standard error */
        STDERR
    }
}
