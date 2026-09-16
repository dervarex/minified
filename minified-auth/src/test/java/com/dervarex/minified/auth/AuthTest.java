package com.dervarex.minified.auth;

import com.dervarex.minified.auth.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Tag;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        AuthManager.init(tempDir);
        AuthManager.resetLoginState();
    }

    @Test
    void resetLoginState_restoresDefaults() {
        LoginState state = AuthManager.getLoginState();
        assertEquals(AuthManager.LoginStatus.IDLE, state.status);
        assertEquals("", state.message);
        assertNull(state.userCode);
        assertNull(state.verificationUri);
        assertNull(state.directVerificationUri);
        assertNull(state.username);
    }

    @Test
    void getLoginStateJson_containsIdleStatus() {
        String json = AuthManager.getLoginStateJson();
        assertNotNull(json);
        assertTrue(json.contains("\"status\":\"IDLE\""));
    }

    @Test
    void hasSessionSaved_returnsFalseByDefault() {
        assertFalse(AuthManager.hasSessionSaved());
    }

    @Test
    void loginWithSavedSession_returnsNullWithoutSession() {
        assertNull(AuthManager.loginWithSavedSession());
    }

    @Test
    void init_createsBaseDirectory() {
        assertTrue(tempDir.toFile().exists());
    }

    @Test
    void init_createsKeyFile() {
        assertTrue(tempDir.resolve("master.key").toFile().exists());
    }

    @Test
    void init_doesNotCreateSessionFile() {
        assertFalse(tempDir.resolve("session.enc").toFile().exists());
    }

    @Test
    void getUser_returnsNullWithoutLogin() {
        assertNull(AuthManager.getUser());
    }

    @Test
    void getLoginState_returnsSnapshotWithSameValues() {
        LoginState first = AuthManager.getLoginState();
        LoginState second = AuthManager.getLoginState();
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.status, second.status);
        assertEquals(first.message, second.message);
    }

    @Test
    void getLoginState_snapshotMutationDoesNotAffectManager() {
        LoginState snapshot = AuthManager.getLoginState();
        snapshot.status = AuthManager.LoginStatus.SUCCESS;
        snapshot.message = "mutated";
        snapshot.userCode = "1234";

        LoginState fresh = AuthManager.getLoginState();
        assertEquals(AuthManager.LoginStatus.IDLE, fresh.status);
        assertEquals("", fresh.message);
        assertNull(fresh.userCode);
    }

    @Test
    void addStateChangeListener_receivesResetNotification() {
        AtomicInteger counter = new AtomicInteger(0);
        com.dervarex.minified.auth.events.LoginStateChangeListener listener =
                state -> counter.incrementAndGet();

        AuthManager.addStateChangeListener(listener);
        AuthManager.resetLoginState();
        assertTrue(counter.get() >= 1);

        AuthManager.removeStateChangeListener(listener);
    }

    @Test
    void removeStateChangeListener_stopsNotifications() {
        AtomicInteger counter = new AtomicInteger(0);
        com.dervarex.minified.auth.events.LoginStateChangeListener listener =
                state -> counter.incrementAndGet();

        AuthManager.addStateChangeListener(listener);
        AuthManager.resetLoginState();
        int before = counter.get();

        AuthManager.removeStateChangeListener(listener);
        AuthManager.resetLoginState();
        assertEquals(before, counter.get());
    }

    @Test
    void addStateChangeListener_multipleListenersAllNotified() {
        AtomicInteger a = new AtomicInteger(0);
        AtomicInteger b = new AtomicInteger(0);

        com.dervarex.minified.auth.events.LoginStateChangeListener la = state -> a.incrementAndGet();
        com.dervarex.minified.auth.events.LoginStateChangeListener lb = state -> b.incrementAndGet();

        AuthManager.addStateChangeListener(la);
        AuthManager.addStateChangeListener(lb);
        AuthManager.resetLoginState();

        assertTrue(a.get() >= 1);
        assertTrue(b.get() >= 1);

        AuthManager.removeStateChangeListener(la);
        AuthManager.removeStateChangeListener(lb);
    }

    @Test
    void resetLoginState_clearsUserCodeAndUris() {
        LoginState state = AuthManager.getLoginState();
        assertNull(state.userCode);
        assertNull(state.verificationUri);
        assertNull(state.directVerificationUri);
    }

    @Test
    void resetLoginState_clearsUsername() {
        LoginState state = AuthManager.getLoginState();
        assertNull(state.username);
    }

    @Test
    void startDeviceCodeLoginAsync_returnsJsonWithStatus() {
        String json = AuthManager.startDeviceCodeLoginAsync();
        assertNotNull(json);
        assertTrue(json.contains("status"));
        AuthManager.resetLoginState();
    }

    @Test
    void startDeviceCodeLoginAsync_doesNotThrow() {
        assertDoesNotThrow(AuthManager::startDeviceCodeLoginAsync);
        AuthManager.resetLoginState();
    }

    @Test
    @Tag("manual")
    void deviceCodeLogin_thenReloadsFromSavedSession() throws Exception {

        System.out.println("Starting device code login. Follow the guide in the terminal");
        AuthManager.startDeviceCodeLoginAsync();

        long timeoutMs = 5L * 60L * 1000L;
        long start = System.currentTimeMillis();
        LoginState state;
        do {
            state = AuthManager.getLoginState();
            if (state.userCode != null && state.verificationUri != null) {
                System.out.println("Go to " + state.verificationUri + " and enter code " + state.userCode);
            }
            if (state.status == AuthManager.LoginStatus.SUCCESS || state.status == AuthManager.LoginStatus.ERROR) {
                break;
            }
            Thread.sleep(500);
        } while (System.currentTimeMillis() - start < timeoutMs);

        state = AuthManager.getLoginState();
        assertEquals(AuthManager.LoginStatus.SUCCESS, state.status, "Login did not complete successfully");
        assertNotNull(AuthManager.getUser());
        assertTrue(AuthManager.hasSessionSaved());

        User reloaded = AuthManager.loginWithSavedSession();
        assertNotNull(reloaded);
        assertNotNull(reloaded.getMinecraftUUID());
        assertNotNull(reloaded.username());
        System.out.println("Serialized Session:");
        System.out.println();
        System.out.println(reloaded.serializedSession());
        System.out.println();
        System.out.println("Reloaded session for " + reloaded.username() + " (" + reloaded.getMinecraftUUID() + ")");
    }
}