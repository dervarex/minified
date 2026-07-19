package com.dervarex.minified.launch.events;

@FunctionalInterface
public interface EventListener<T extends Event> {
    void onEvent(T event);
}