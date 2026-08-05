package com.dervarex.minified.auth;

public class LoginState {
    public AuthManager.LoginStatus status = AuthManager.LoginStatus.IDLE;
    public String message = "";
    public String userCode = null;
    public String verificationUri = null;
    public String directVerificationUri = null;
    public String username = null;

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