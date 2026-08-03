package com.dervarex.minified.events.type.connection;

import com.dervarex.minified.events.Event;

/**
 * Gets fired when we're checking whether the user is online or not
 */
public record CheckConnectionEvent() implements Event {}