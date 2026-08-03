package com.dervarex.minified.launch.events.launch;

import com.dervarex.minified.auth.User;
import com.dervarex.minified.events.Event;
import com.dervarex.minified.launch.launch.LaunchConfiguration;

public record GameStartEvent(
        User user,
        LaunchConfiguration launchConfiguration,
        boolean online
) implements Event {
}