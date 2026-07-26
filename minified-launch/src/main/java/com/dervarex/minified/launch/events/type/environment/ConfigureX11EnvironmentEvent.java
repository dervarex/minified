package com.dervarex.minified.launch.events.type.environment;

import com.dervarex.minified.launch.events.Event;

public record ConfigureX11EnvironmentEvent(String display) implements Event { }