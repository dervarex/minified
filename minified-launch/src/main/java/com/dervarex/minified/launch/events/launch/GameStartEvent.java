package com.dervarex.minified.launch.events.launch;

import com.dervarex.minified.auth.user.User;
import com.dervarex.minified.events.Event;
import com.dervarex.minified.launch.launch.LaunchConfiguration;

/**
 * Gets fired when Minecraft is starting
 *
 * @param user                the user that was used to launch the game
 * @param launchConfiguration the launch configuration that was used to launch the game
 * @param online              whether the connectivity check earlier in the launch succeeded
 */
public record GameStartEvent(
        User user,
        LaunchConfiguration launchConfiguration,
        boolean online
) implements Event {
}