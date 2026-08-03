package com.dervarex.minified.launch.events.launch;

import com.dervarex.minified.events.Event;
import com.dervarex.minified.launch.launch.LaunchConfiguration;

public record GameStoppedEvent(int exitCode, LaunchConfiguration launchConfiguration) implements Event { }