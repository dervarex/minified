package com.dervarex.minified.launch.events.launch;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import org.apiguardian.api.API;

/**
 * Gets fired right after the Minecraft process was spawned, so a launcher can keep a handle to it (to stop it, for example)
 *
 * @param process             the running game process
 * @param launchConfiguration the launch configuration that was used to launch the game
 */
@API(status = API.Status.STABLE)
public record GameProcessStartedEvent(Process process, LaunchConfiguration launchConfiguration) implements Event { }
