package com.dervarex.minified.auth;

public class LoginState {
    public AuthManager.LoginStatus status = AuthManager.LoginStatus.IDLE;
    public String message = "";
    public String userCode = null;
    public String verificationUri = null;
    public String directVerificationUri = null;
    public String username = null;
}