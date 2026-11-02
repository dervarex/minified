package com.dervarex.minified.launch.events.environment;

import com.dervarex.minified.events.Event;
import org.apiguardian.api.API;

/**
 * Gets fired when the X11 Environment gets configured, only on Linux when using Wayland
 *
 * @param display the display that has been resolved
 */
@API(status = API.Status.STABLE)
public record ConfigureX11EnvironmentEvent(String display) implements Event { }