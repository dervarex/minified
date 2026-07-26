package com.dervarex.minified.launch.events.type.launch;

import com.dervarex.minified.launch.events.Event;
import com.dervarex.minified.launch.launch.LaunchConfiguration;

public record GameStoppedEvent(int exitCode, LaunchConfiguration launchConfiguration) implements Event { }