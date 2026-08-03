package com.dervarex.minified.launch.events.environment;

import com.dervarex.minified.events.Event;

public record ConfigureX11EnvironmentEvent(String display) implements Event { }