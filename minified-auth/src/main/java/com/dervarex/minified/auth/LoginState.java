package com.dervarex.minified.auth;

import org.apiguardian.api.API;

/**
 * Current Login State <br>
 * <pre>
 * {@link status} - {@link AuthManager.LoginStatus}
 * {@link message} - Current Message from the Backend, can be shown to the user.
 * {@link userCode} - Code for Login on Microsoft's Site.
 * {@link verificationUri} - Link to Microsoft's site where the user needs to enter the {@link userCode}.
 * {@link directVerificationUri} - Same as {@link verificationUri}, but with the Code pre-entered.
 * {@link username} - When successfully logged in, that's the username.
 * </pre>
 */
@API(status = API.Status.STABLE)
public class LoginState {

    public volatile AuthManager.LoginStatus status = AuthManager.LoginStatus.IDLE;

    public volatile String message = "";
    public volatile String userCode = null;
    public volatile String verificationUri = null;
    public volatile String directVerificationUri = null;
    public volatile String username = null;

    public LoginState() {}

    // Copy Constructor for Thread Safety
    public LoginState(LoginState other) {
        this.status = other.status;
        this.message = other.message;
        this.userCode = other.userCode;
        this.verificationUri = other.verificationUri;
        this.directVerificationUri = other.directVerificationUri;
        this.username = other.username;
    }
}