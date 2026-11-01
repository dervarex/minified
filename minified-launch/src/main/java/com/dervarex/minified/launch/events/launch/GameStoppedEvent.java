package com.dervarex.minified.launch.events.launch;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.launch.launch.LaunchConfiguration;
import org.apiguardian.api.API;

/**
 * Gets fired when the Minecraft Process has been stopped
 *
 * @param exitCode            the exit code that was returned by the process
 * @param launchConfiguration the launch configuration that was used to launch the game
 */
@API(status = API.Status.STABLE)
public record GameStoppedEvent(int exitCode, LaunchConfiguration launchConfiguration) implements Event { }