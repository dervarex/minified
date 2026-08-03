package com.dervarex.minified.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {
    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    public <T extends Event> void subscribe(Class<T> type, EventListener<T> listener) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public <T extends Event> void unsubscribe(
            Class<T> type,
            EventListener<T> listener
    ) {
        List<EventListener<?>> eventListeners = listeners.get(type);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
        if (eventListeners != null && eventListeners.isEmpty()) {
            listeners.remove(type);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void post(T event) {
        for (EventListener<?> listener : listeners.getOrDefault(event.getClass(), List.of())) {
            ((EventListener<T>) listener).onEvent(event);
        }
    }

}