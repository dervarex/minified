package com.dervarex.minified.events;

import com.dervarex.minified.events.type.connection.CheckConnectionEvent;
import com.dervarex.minified.events.type.connection.OfflineEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EventBusTest {

    private final EventBus eventBus = new EventBus();
    private final List<String> heard = new ArrayList<>();

    @Test
    void everyListenerHearsItsEventInOrder() {
        eventBus.subscribe(CheckConnectionEvent.class, event -> heard.add("first"));
        eventBus.subscribe(CheckConnectionEvent.class, event -> heard.add("second"));

        eventBus.post(new CheckConnectionEvent());

        assertEquals(List.of("first", "second"), heard);
    }

    @Test
    void listenersOnlyHearTheirOwnEvent() {
        eventBus.subscribe(CheckConnectionEvent.class, event -> heard.add("checking"));

        eventBus.post(new OfflineEvent());

        assertEquals(List.of(), heard);
    }

    @Test
    void unsubscribedListenersHearNothing() {
        EventListener<OfflineEvent> listener = event -> heard.add("offline");
        eventBus.subscribe(OfflineEvent.class, listener);
        eventBus.unsubscribe(OfflineEvent.class, listener);

        eventBus.post(new OfflineEvent());

        assertEquals(List.of(), heard);
        assertDoesNotThrow(() -> eventBus.unsubscribe(OfflineEvent.class, listener));
    }

    @Test
    void listenersCanUnsubscribeThemselvesWhileBeingCalled() {
        eventBus.subscribe(OfflineEvent.class, new EventListener<>() {
            @Override
            public void onEvent(OfflineEvent event) {
                heard.add("once");
                eventBus.unsubscribe(OfflineEvent.class, this);
            }
        });

        eventBus.post(new OfflineEvent());
        eventBus.post(new OfflineEvent());

        assertEquals(List.of("once"), heard);
    }

    @Test
    void oneBrokenListenerDoesNotRuinItForTheOthers() {
        eventBus.subscribe(OfflineEvent.class, event -> {
            throw new IllegalStateException("some gui listener having a bad day");
        });
        eventBus.subscribe(OfflineEvent.class, event -> heard.add("still here"));

        assertDoesNotThrow(() -> eventBus.post(new OfflineEvent()));
        assertEquals(List.of("still here"), heard);
    }
}
