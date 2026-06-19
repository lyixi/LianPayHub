package com.lianpayhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lianpayhub.admin")
public class DefaultAdminProperties {

    private String defaultUsername = "admin";
    private String defaultPassword = "admin123456";
    private boolean forceDefaultPasswordChange = false;
    private boolean passwordComplexityRequired = false;
    private boolean exportEnabled = true;

    public String getDefaultUsername() {
        return defaultUsername;
    }

    public void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public boolean isForceDefaultPasswordChange() {
        return forceDefaultPasswordChange;
    }

    public void setForceDefaultPasswordChange(boolean forceDefaultPasswordChange) {
        this.forceDefaultPasswordChange = forceDefaultPasswordChange;
    }

    public boolean isPasswordComplexityRequired() {
        return passwordComplexityRequired;
    }

    public void setPasswordComplexityRequired(boolean passwordComplexityRequired) {
        this.passwordComplexityRequired = passwordComplexityRequired;
    }

    public boolean isExportEnabled() {
        return exportEnabled;
    }

    public void setExportEnabled(boolean exportEnabled) {
        this.exportEnabled = exportEnabled;
    }
}
