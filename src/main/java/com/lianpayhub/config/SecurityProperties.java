package com.lianpayhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lianpayhub.security")
public class SecurityProperties {

    private String jwtSecret;
    private Integer jwtExpireMinutes = 720;
    private Boolean smsCodeRequired = false;
    private Boolean apiAuthEnabled = false;
    private String apiAuthMode = "secret";
    private Integer apiSignatureTimeWindowSeconds = 300;

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
}
