package com.dervarex.minified.launch.events.type.launch;

import com.dervarex.minified.auth.User;
import com.dervarex.minified.launch.events.Event;
import com.dervarex.minified.launch.launch.LaunchConfiguration;

public record LaunchStartedEvent(
        User user,
        LaunchConfiguration launchConfiguration,
        boolean online
) implements Event {
}