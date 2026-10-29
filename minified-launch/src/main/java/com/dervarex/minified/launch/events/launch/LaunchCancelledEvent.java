package com.dervarex.minified.launch.events.launch;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import org.apiguardian.api.API;

/**
 * Gets fired when a {@link com.dervarex.minified.launch.events.control.StopGameEvent StopGameEvent} stopped the launch
 * before the game was started. {@code GameStoppedEvent} doesn't fire then there was no game yet
 *
 * @param launchConfiguration the launch configuration that was used to launch the game
 */
@API(status = API.Status.EXPERIMENTAL)
public record LaunchCancelledEvent(LaunchConfiguration launchConfiguration) implements Event {

}
