package com.dervarex.minified.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Tag;

import java.nio.file.Path;

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
    void resetLoginStateRestoresDefaults() {
        LoginState state = AuthManager.getLoginState();
        assertEquals(AuthManager.LoginStatus.IDLE, state.status);
        assertEquals("", state.message);
        assertNull(state.userCode);
        assertNull(state.verificationUri);
        assertNull(state.directVerificationUri);
        assertNull(state.username);
    }

    @Test
    void loginStateJsonContainsStatus() {
        String json = AuthManager.getLoginStateJson();
        assertNotNull(json);
        assertTrue(json.contains("\"status\":\"IDLE\""));
    }

    @Test
    void noSessionExistsByDefault() {
        assertFalse(AuthManager.hasSessionSaved());
        assertNull(AuthManager.loginWithSavedSession());
    }

    @Test
    @Tag("manual")
    void deviceCodeLoginThenReloadsFromSavedSession() throws Exception {

        System.out.println("Starting device code login. Follow the printed URL/code in the console.");
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
        assertNotNull(reloaded.uuid());
        assertNotNull(reloaded.username());
        System.out.println("Serialized Session:");
        System.out.println();
        System.out.println(reloaded.serializedSession());
        System.out.println();
        System.out.println("Reloaded session for " + reloaded.username() + " (" + reloaded.uuid() + ")");
    }
}
