package com.minimarket.security.model;

import java.util.List;

public class TokenPairResponse {

    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private long expiresIn;
    private String username;
    private List<String> roles;
    private boolean mfaRequired;
    private String mfaToken;

    public TokenPairResponse() {
    }

    public static TokenPairResponse accessAndRefresh(
            String accessToken, String refreshToken, long expiresIn,
            String username, List<String> roles) {
        TokenPairResponse response = new TokenPairResponse();
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;
        response.expiresIn = expiresIn;
        response.username = username;
        response.roles = roles;
        response.mfaRequired = false;
        return response;
    }

    public static TokenPairResponse mfaChallenge(String mfaToken) {
        TokenPairResponse response = new TokenPairResponse();
        response.mfaRequired = true;
        response.mfaToken = mfaToken;
        return response;
    }

    /** Compatibilidad con clientes que leen "token". */
    public String getToken() {
        return accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
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
