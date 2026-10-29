package com.dervarex.minified.launch.events.control;

import com.dervarex.minified.events.ReverseEvent;
import org.apiguardian.api.API;

/**
 * Post this to stop the running game, for a "Stop Game" button for example.
 * Not to be confused with {@link com.dervarex.minified.launch.events.launch.GameStoppedEvent GameStoppedEvent},
 * that one fires once the Game has Stopped.
 * <br>
 * Every launch with the same {@code EventBus} gets stopped.
 * A game stopped this way doesn't throw a {@code NonZeroExitCodeException}, but {@code GameStoppedEvent} still fires
 *
 * @param force true kills the process, false asks the game to close first. attention: killing the game can cause data loss!
 */
@API(status = API.Status.EXPERIMENTAL)
public record StopGameEvent(boolean force) implements ReverseEvent {

    /**
     * Asks the game to close, without forcing it
     */
    public StopGameEvent() {
        this(false);
    }
}
