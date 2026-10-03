package com.dervarex.minified.utils.exceptions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoConnectionExceptionTest {

    @Test
    void suggestsACaptivePortalWhenOnlyHttpFails() {
        NoConnectionException e = new NoConnectionException.Builder()
                .dnsResolved(true)
                .tcpAny(true)
                .httpAny(false)
                .build();

        assertTrue(e.getSuggestions().contains("You might be on a captive portal(hotels often have that)"));
        assertFalse(e.getSuggestions().contains("Check DNS and Router"));
    }

    @Test
    void ownSuggestionsReplaceTheDefaultOnes() {
        NoConnectionException e = new NoConnectionException.Builder()
                .addSuggestion("Plug the cable back in")
                .addSuggestion("   ")
                .build();

        assertEquals(List.of("Plug the cable back in"), e.getSuggestions());
    }

    @Test
    void throwIfOfflineOnlyThrowsWhenTheCheckFails() {
        assertDoesNotThrow(() -> NoConnectionException.throwIfOffline("launch", () -> true));

        NoConnectionException e = assertThrows(NoConnectionException.class,
                () -> NoConnectionException.throwIfOffline("launch", () -> false));
        assertEquals("launch", e.getActionContext());
        // a check that explodes counts as offline too
        assertThrows(NoConnectionException.class,
                () -> NoConnectionException.throwIfOffline("launch", () -> { throw new IllegalStateException("no wifi"); }));
    }
}
