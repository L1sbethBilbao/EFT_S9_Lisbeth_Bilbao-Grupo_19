package com.minimarket.security.model;

import java.util.List;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private long expiresIn;
    private String username;
    private List<String> roles;

    public AuthResponse() {
    }

    public AuthResponse(String token, long expiresIn, String username, List<String> roles) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.username = username;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
}
