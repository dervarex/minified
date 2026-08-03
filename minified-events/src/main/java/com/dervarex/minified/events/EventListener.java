package com.dervarex.minified.events;

@FunctionalInterface
public interface EventListener<T extends Event> {
    void onEvent(T event);
}