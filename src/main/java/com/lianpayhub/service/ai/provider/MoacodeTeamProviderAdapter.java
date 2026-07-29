package com.lianpayhub.service.ai.provider;

import org.springframework.stereotype.Component;

@Component
public class MoacodeTeamProviderAdapter extends MoacodeProviderAdapter {

    @Override
    public String providerCode() {
        return "moacode-team";
    }

    @Override
    public boolean supports(String providerCode) {
        return "moacode-team".equalsIgnoreCase(providerCode);
    }
}
