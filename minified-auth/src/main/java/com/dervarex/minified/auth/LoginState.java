package com.dervarex.minified.auth;
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