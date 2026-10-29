package com.dervarex.minified.events;

import org.apiguardian.api.API;

@FunctionalInterface
@API(status = API.Status.STABLE)
public interface EventListener<T extends Event> {
    void onEvent(T event);
}