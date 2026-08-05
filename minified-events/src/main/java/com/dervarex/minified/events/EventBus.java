package com.dervarex.minified.events;

import org.apiguardian.api.API;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {
    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    /**
     * Example usage:
     * <pre>{@code
     * // replace SomeEvent with any event you'd like to listen to
     * eventBus.subscribe(SomeEvent.class, event -> {
     *     // Code here will get executed when the event gets triggered
     * });
     * }</pre>
     */
    @API(status = API.Status.STABLE)
    public <T extends Event> void subscribe(Class<T> type, EventListener<T> listener) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @API(status = API.Status.STABLE)
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
    @API(status = API.Status.STABLE)
    public <T extends Event> void post(T event) {
        for (EventListener<?> listener : listeners.getOrDefault(event.getClass(), List.of())) {
            ((EventListener<T>) listener).onEvent(event);
        }
    }

}