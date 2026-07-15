package com.minimarket.security.model;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {

    @NotBlank(message = "El refresh token es obligatorio para cerrar sesión")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
