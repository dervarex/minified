package com.dervarex.minified.launch.events.type.connection;

import com.dervarex.minified.launch.events.Event;

/**
 * Gets fired when we're checking whether the user is online or not
 */
public record CheckConnectionEvent() implements Event {}