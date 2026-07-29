package com.lianpayhub.service.ai.provider;

import com.lianpayhub.domain.ai.AiProviderConfig;
import com.lianpayhub.domain.ai.AppAiProviderSetting;
import com.lianpayhub.domain.ai.UserAiCredential;
import com.lianpayhub.service.ai.AiChatResult;

public interface AiProviderAdapter {
    String providerCode();
    boolean supports(String providerCode);
    String createUserKey(AiProviderConfig providerConfig, AppAiProviderSetting appSetting, Long userId);
    AiChatResult chat(AiProviderConfig providerConfig, UserAiCredential credential, String model, String message, boolean stream, String imageUrl);
}
