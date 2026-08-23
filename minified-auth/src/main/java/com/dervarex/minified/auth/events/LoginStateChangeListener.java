package com.dervarex.minified.auth.events;

import com.dervarex.minified.auth.LoginState;

/**
 * Can be used like this:
 * <pre>{@code
 * AuthManager.addStateChangeListener(newState -> {
 *     System.out.println("New Login State: " + newState.status);
 * });
 * }</pre>
 */
@FunctionalInterface
public interface LoginStateChangeListener {
    void onStateChanged(LoginState newState);
}