package com.minimarket.security.retention;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "data.retention")
public class DataRetentionProperties {

    private boolean enabled = true;
    private int inactiveDays = 90;
    private String cron = "0 0 2 * * *";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getInactiveDays() {
        return inactiveDays;
    }

    public void setInactiveDays(int inactiveDays) {
        this.inactiveDays = inactiveDays;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }
}
