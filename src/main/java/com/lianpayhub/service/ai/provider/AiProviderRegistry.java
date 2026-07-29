package com.lianpayhub.service.ai.provider;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiProviderRegistry {
    private final List<AiProviderAdapter> adapters;

    public AiProviderRegistry(List<AiProviderAdapter> adapters) {
        this.adapters = adapters;
    }

    public AiProviderAdapter require(String providerCode) {
        for (AiProviderAdapter adapter : adapters) {
            if (adapter.supports(providerCode)) return adapter;
        }
        throw new BusinessException(ErrorCode.NOT_FOUND, "AI平台适配器不存在: " + providerCode);
    }

    public List<String> supportedProviderCodes() {
        List<String> codes = new ArrayList<String>();
        for (AiProviderAdapter adapter : adapters) {
            codes.add(adapter.providerCode());
        }
        return codes;
    }
}
