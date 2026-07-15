package com.minimarket.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {

    private int authPerMinute = 30;

    public int getAuthPerMinute() {
        return authPerMinute;
    }

    public void setAuthPerMinute(int authPerMinute) {
        this.authPerMinute = authPerMinute;
    }
}
