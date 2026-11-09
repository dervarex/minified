package com.dervarex.minified.auth;

import org.apiguardian.api.API;

/**
 * Current login state
 */
@API(status = API.Status.STABLE)
public class LoginState {

    /** Where the login currently is */
    public volatile AuthManager.LoginStatus status = AuthManager.LoginStatus.IDLE;

    /** Current message from the backend, can be shown to the user */
    public volatile String message = "";
    /** Code for the login on Microsoft's site */
    public volatile String userCode = null;
    /** Link to Microsoft's site where the user needs to enter the {@link #userCode} */
    public volatile String verificationUri = null;
    /** Same as {@link #verificationUri}, but with the code pre-entered */
    public volatile String directVerificationUri = null;
    /** The username, once logged in */
    public volatile String username = null;

    /**
     * Creates an idle login state
     */
    public LoginState() {}

    /**
     * Copies another login state
     *
     * @param other the state to copy
     */
    public LoginState(LoginState other) {
        this.status = other.status;
        this.message = other.message;
        this.userCode = other.userCode;
        this.verificationUri = other.verificationUri;
        this.directVerificationUri = other.directVerificationUri;
        this.username = other.username;
    }
}