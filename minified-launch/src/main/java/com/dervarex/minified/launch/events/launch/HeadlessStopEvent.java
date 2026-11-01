package com.dervarex.minified.launch.events.launch;

import com.dervarex.minified.events.Event;

/**
 * Fired in headless mode when the game is stopped or stops on its own, before {@link GameStoppedEvent}. <br>
 * The game's own output goes to {@link GameOutputEvent}
 *
 * @param reason why the game stopped
 */
public record HeadlessStopEvent(Reason reason) implements Event {

    public enum Reason {
        /** The game reached a headless marker and was stopped, success */
        MARKER_REACHED,
        /** The game crashed */
        CRASHED,
        /** No marker was reached within the headless timeout, timeout*/
        TIMEOUT,
        /** The game exited by itself shortly after reaching a marker, before we could confirm it worked*/
        EXITED_AFTER_MARKER,
        /** The game exited on its own before reaching a marker */
        EXITED_BEFORE_MARKER,
        /** Fired in addition to another reason when the game hit a timeout and had to be killed */
        FORCE_KILLED
    }
}
