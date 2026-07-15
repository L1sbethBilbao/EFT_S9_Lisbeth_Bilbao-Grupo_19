package com.minimarket.security.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MfaSetupResponse {

    private String secret;
    private String qrUri;

    public MfaSetupResponse() {
    }

    public MfaSetupResponse(String secret, String qrUri) {
        this.secret = secret;
        this.qrUri = qrUri;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getQrUri() {
        return qrUri;
    }

    public void setQrUri(String qrUri) {
        this.qrUri = qrUri;
    }
}
