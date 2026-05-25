package com.lianpayhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lianpayhub.payment")
public class PaymentProperties {

    private Boolean devToolsEnabled = true;

    public Boolean getDevToolsEnabled() {
        return devToolsEnabled;
    }

    public void setDevToolsEnabled(Boolean devToolsEnabled) {
        this.devToolsEnabled = devToolsEnabled;
    }
}
