package com.dervarex.minified.launch.events.launch;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

/**
 * Gets fired when the Game's exit code isn't zero, which most likely indicates a crash or any other issues
 */
@API(status = API.Status.STABLE)
public record NonZeroExitCodeEvent(int exitCode) implements Event {
}
