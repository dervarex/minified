package com.dervarex.minified.events;

import org.apiguardian.api.API;

/**
 * Listens to one type of event
 *
 * @param <T> the event type
 */
@FunctionalInterface
@API(status = API.Status.STABLE)
public interface EventListener<T extends Event> {
    /**
     * Gets called when the event is posted
     *
     * @param event the event
     */
    void onEvent(T event);
}