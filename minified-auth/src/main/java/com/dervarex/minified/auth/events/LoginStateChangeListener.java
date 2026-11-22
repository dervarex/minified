package com.dervarex.minified.auth.events;

import com.dervarex.minified.auth.LoginState;
import org.apiguardian.api.API;

/**
 * Can be used like this:
 * <pre>{@code
 * AuthManager.addStateChangeListener(newState -> {
 *     System.out.println("New Login State: " + newState.status);
 * });
 * }</pre>
 */
@FunctionalInterface
@API(status = API.Status.STABLE)
public interface LoginStateChangeListener {
    /**
     * Gets called when the login state changes, on the thread that changed it (usually the login thread, so
     * hand the work to your UI thread if needed)
     *
     * @param newState a snapshot of the new state; it won't change afterwards, so it's safe to keep
     */
    void onStateChanged(LoginState newState);
}