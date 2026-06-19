package com.lianpayhub.web.admin;

import java.util.List;

public class PaymentConfigCheckResult {
    private final boolean ready;
    private final String suggestedNotifyPath;
    private final List<String> warnings;

    public PaymentConfigCheckResult(boolean ready, String suggestedNotifyPath, List<String> warnings) {
        this.ready = ready;
        this.suggestedNotifyPath = suggestedNotifyPath;
        this.warnings = warnings;
    }

    public boolean isReady() { return ready; }
    public String getSuggestedNotifyPath() { return suggestedNotifyPath; }
    public List<String> getWarnings() { return warnings; }
}
