package com.dervarex.minified.auth;

import com.dervarex.minified.auth.events.LoginStateChangeListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginStateListenerTest {

    private final List<LoginStateChangeListener> added = new ArrayList<>();

    private void listen(LoginStateChangeListener listener) {
        added.add(listener);
        AuthManager.addStateChangeListener(listener);
    }

    @AfterEach
    void removeListeners() {
        added.forEach(AuthManager::removeStateChangeListener);
    }

    @Test
    void listenersGetASnapshotThatDoesntChangeLater() {
        List<LoginState> seen = new ArrayList<>();
        listen(seen::add);

        LoginState live = new LoginState();
        live.status = AuthManager.LoginStatus.PENDING;
        live.userCode = "ABCD-1234";
        AuthManager.notifyStateChanged(live);
        live.status = AuthManager.LoginStatus.SUCCESS;

        assertEquals(1, seen.size());
        assertNotSame(live, seen.getFirst());
        assertEquals(AuthManager.LoginStatus.PENDING, seen.getFirst().status);
        assertEquals("ABCD-1234", seen.getFirst().userCode);
    }

    @Test
    void aThrowingListenerDoesntStopTheOthersAndIsReported() {
        List<Throwable> reported = new ArrayList<>();
        Thread thread = Thread.currentThread();
        Thread.UncaughtExceptionHandler previous = thread.getUncaughtExceptionHandler();
        thread.setUncaughtExceptionHandler((t, e) -> reported.add(e));
        try {
            List<LoginState> seen = new ArrayList<>();
            listen(state -> {
                throw new IllegalStateException("broken UI listener");
            });
            listen(seen::add);

            assertDoesNotThrow(() -> AuthManager.notifyStateChanged(new LoginState()));
            assertEquals(1, seen.size());
            assertEquals(1, reported.size());
            assertEquals("broken UI listener", reported.getFirst().getMessage());
        } finally {
            thread.setUncaughtExceptionHandler(previous);
        }
    }

    @Test
    void inProgressMeansStartingOrPending() {
        LoginState state = new LoginState();
        for (AuthManager.LoginStatus status : AuthManager.LoginStatus.values()) {
            state.status = status;
            boolean expected = status == AuthManager.LoginStatus.STARTING || status == AuthManager.LoginStatus.PENDING;
            assertEquals(expected, state.inProgress(), status.name());
        }
        assertFalse(new LoginState().inProgress());
        state.status = AuthManager.LoginStatus.PENDING;
        assertTrue(new LoginState(state).inProgress());
    }
}
