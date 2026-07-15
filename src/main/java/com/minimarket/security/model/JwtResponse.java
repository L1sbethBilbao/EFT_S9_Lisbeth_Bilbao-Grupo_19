package com.minimarket.security.model;

public class JwtResponse {

    private String token;
    private boolean mfaRequired;
    private String mfaToken;

    public JwtResponse() {
    }

    public JwtResponse(String token) {
        this.token = token;
        this.mfaRequired = false;
    }

    public static JwtResponse mfaChallenge(String mfaToken) {
        JwtResponse response = new JwtResponse();
        response.mfaRequired = true;
        response.mfaToken = mfaToken;
        return response;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isMfaRequired() {
        return mfaRequired;
    }

    public void setMfaRequired(boolean mfaRequired) {
        this.mfaRequired = mfaRequired;
    }

    public String getMfaToken() {
        return mfaToken;
    }

    public void setMfaToken(String mfaToken) {
        this.mfaToken = mfaToken;
    }
}
