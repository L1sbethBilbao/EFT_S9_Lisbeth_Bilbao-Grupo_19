package com.minimarket.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.mfa")
public class MfaProperties {

    private String issuer = "MiniMarketPlus";
    private long tokenExpirationMs = 300000;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getTokenExpirationMs() {
        return tokenExpirationMs;
    }

    public void setTokenExpirationMs(long tokenExpirationMs) {
        this.tokenExpirationMs = tokenExpirationMs;
    }
}
