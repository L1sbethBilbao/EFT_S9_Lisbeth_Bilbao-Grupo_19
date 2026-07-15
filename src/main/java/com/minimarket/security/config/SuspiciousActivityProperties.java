package com.minimarket.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.monitor")
public class SuspiciousActivityProperties {

    private int failedLoginThreshold = 5;
    private int requestThreshold = 200;
    private int windowMinutes = 15;

    public int getFailedLoginThreshold() {
        return failedLoginThreshold;
    }

    public void setFailedLoginThreshold(int failedLoginThreshold) {
        this.failedLoginThreshold = failedLoginThreshold;
    }

    public int getRequestThreshold() {
        return requestThreshold;
    }

    public void setRequestThreshold(int requestThreshold) {
        this.requestThreshold = requestThreshold;
    }

    public int getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(int windowMinutes) {
        this.windowMinutes = windowMinutes;
    }

    public long getWindowMs() {
        return windowMinutes * 60L * 1000L;
    }
}
