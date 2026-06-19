package com.lianpayhub.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lianpayhub.security")
public class SecurityProperties {

    private String jwtSecret;
    private Integer jwtExpireMinutes = 720;
    private Boolean smsCodeRequired = false;
    private Integer smsCodeExpireMinutes = 5;
    private Integer smsCodeCooldownSeconds = 60;
    private Integer smsCodeMaxAttempts = 5;
    private Boolean smsDebugReturnCode = true;
    private String smsProvider = "aliyun";
    private Boolean apiAuthEnabled = false;
    private String apiAuthMode = "secret";
    private Integer apiSignatureTimeWindowSeconds = 300;
    private List<String> adminIpWhitelist = new ArrayList<>();

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public Integer getJwtExpireMinutes() {
        return jwtExpireMinutes;
    }

    public void setJwtExpireMinutes(Integer jwtExpireMinutes) {
        this.jwtExpireMinutes = jwtExpireMinutes;
    }

    public Boolean getSmsCodeRequired() {
        return smsCodeRequired;
    }

    public void setSmsCodeRequired(Boolean smsCodeRequired) {
        this.smsCodeRequired = smsCodeRequired;
    }

    public Integer getSmsCodeExpireMinutes() {
        return smsCodeExpireMinutes;
    }

    public void setSmsCodeExpireMinutes(Integer smsCodeExpireMinutes) {
        this.smsCodeExpireMinutes = smsCodeExpireMinutes;
    }

    public Integer getSmsCodeCooldownSeconds() {
        return smsCodeCooldownSeconds;
    }

    public void setSmsCodeCooldownSeconds(Integer smsCodeCooldownSeconds) {
        this.smsCodeCooldownSeconds = smsCodeCooldownSeconds;
    }

    public Integer getSmsCodeMaxAttempts() {
        return smsCodeMaxAttempts;
    }

    public void setSmsCodeMaxAttempts(Integer smsCodeMaxAttempts) {
        this.smsCodeMaxAttempts = smsCodeMaxAttempts;
    }

    public Boolean getSmsDebugReturnCode() {
        return smsDebugReturnCode;
    }

    public void setSmsDebugReturnCode(Boolean smsDebugReturnCode) {
        this.smsDebugReturnCode = smsDebugReturnCode;
    }

    public String getSmsProvider() {
        return smsProvider;
    }

    public void setSmsProvider(String smsProvider) {
        this.smsProvider = smsProvider;
    }

    public Boolean getApiAuthEnabled() {
        return apiAuthEnabled;
    }

    public void setApiAuthEnabled(Boolean apiAuthEnabled) {
        this.apiAuthEnabled = apiAuthEnabled;
    }

    public String getApiAuthMode() {
        return apiAuthMode;
    }

    public void setApiAuthMode(String apiAuthMode) {
        this.apiAuthMode = apiAuthMode;
    }

    public Integer getApiSignatureTimeWindowSeconds() {
        return apiSignatureTimeWindowSeconds;
    }

    public void setApiSignatureTimeWindowSeconds(Integer apiSignatureTimeWindowSeconds) {
        this.apiSignatureTimeWindowSeconds = apiSignatureTimeWindowSeconds;
    }

    public List<String> getAdminIpWhitelist() {
        return adminIpWhitelist;
    }

    public void setAdminIpWhitelist(List<String> adminIpWhitelist) {
        this.adminIpWhitelist = adminIpWhitelist == null ? new ArrayList<>() : adminIpWhitelist;
    }
}
