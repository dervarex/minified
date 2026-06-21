package com.dervarex.minified.launch;

import com.dervarex.minified.auth.AuthManager;
import com.dervarex.minified.auth.LoginState;
import com.dervarex.minified.auth.User;

public final class AuthTest {

    private AuthTest() {
    }

    public static User auth() {
        AuthManager.init(TestEnvironment.authDirectory());

        if (AuthManager.hasSessionSaved()) {
            try {
                User user = AuthManager.loginWithSavedSession();
                if (user != null) {
                    System.out.println("Auto-login successful:");
                    System.out.println(user.getUsername() + " (" + user.getUuid() + ")");
                    return user;
                }
            } catch (Exception e) {
                System.out.println("Saved session invalid.");
                e.printStackTrace();
            }
        }

        System.out.println("Starting Microsoft login...");
        return loginWithMicrosoft();
    }

    private static User loginWithMicrosoft() {
        AuthManager.startDeviceCodeLoginAsync();

        long timeoutMs = 5L * 60L * 1000L;
        long start = System.currentTimeMillis();

        LoginState state;

        do {
            state = AuthManager.getLoginState();

            if (state.userCode != null && state.verificationUri != null) {
                System.out.println(
                        "Go to " + state.verificationUri +
                                " and enter code " + state.userCode
                );
            }

            if (state.status == AuthManager.LoginStatus.SUCCESS ||
                    state.status == AuthManager.LoginStatus.ERROR) {
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Login interrupted.", e);
            }

        } while (System.currentTimeMillis() - start < timeoutMs);

        state = AuthManager.getLoginState();

        if (state.status != AuthManager.LoginStatus.SUCCESS) {
            throw new IllegalStateException("Login failed: " + state.status);
        }

        User user = AuthManager.getUser();
        if (user == null) {
            throw new IllegalStateException("Login succeeded, but no user was returned.");
        }

        System.out.println("Logged in as:");
        System.out.println(user.getUsername() + " (" + user.getUuid() + ")");
        return user;
    }
}