package com.lianpayhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lianpayhub.payment")
public class PaymentProperties {

    private Boolean devToolsEnabled = true;
    private Integer orderExpireMinutes = 30;
    private Boolean orderAutoCloseEnabled = true;
    private Long orderCloseScanFixedDelayMs = 60000L;

    public Boolean getDevToolsEnabled() {
        return devToolsEnabled;
    }

    public void setDevToolsEnabled(Boolean devToolsEnabled) {
        this.devToolsEnabled = devToolsEnabled;
    }

    public Integer getOrderExpireMinutes() {
        return orderExpireMinutes;
    }

    public void setOrderExpireMinutes(Integer orderExpireMinutes) {
        this.orderExpireMinutes = orderExpireMinutes;
    }

    public Boolean getOrderAutoCloseEnabled() {
        return orderAutoCloseEnabled;
    }

    public void setOrderAutoCloseEnabled(Boolean orderAutoCloseEnabled) {
        this.orderAutoCloseEnabled = orderAutoCloseEnabled;
    }

    public Long getOrderCloseScanFixedDelayMs() {
        return orderCloseScanFixedDelayMs;
    }

    public void setOrderCloseScanFixedDelayMs(Long orderCloseScanFixedDelayMs) {
        this.orderCloseScanFixedDelayMs = orderCloseScanFixedDelayMs;
    }
}
