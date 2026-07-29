package com.lianpayhub.service.ai;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.ai.UserAiCredential;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.UserAiCredentialRepository;
import com.lianpayhub.repository.UserInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAiCredentialAdminService {
    private final UserAiCredentialRepository repository;
    private final UserInfoRepository userInfoRepository;
    private final AppInfoRepository appInfoRepository;

    public UserAiCredentialAdminService(UserAiCredentialRepository repository, UserInfoRepository userInfoRepository,
                                        AppInfoRepository appInfoRepository) {
        this.repository = repository;
        this.userInfoRepository = userInfoRepository;
        this.appInfoRepository = appInfoRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserAiCredential> list(String appId, Pageable pageable) {
        return appId == null || appId.trim().isEmpty() ? repository.findAll(pageable) : repository.findByAppId(appId, pageable);
    }

    @Transactional(readOnly = true)
    public java.util.List<UserAiCredential> listByUser(Long userId) {
        return repository.findByUserIdOrderByIdDesc(userId);
    }

    @Transactional
    public UserAiCredential upsert(Long userId, String appId, String providerCode, String apiKey, Long quotaUnits) {
        UserInfo user = userInfoRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        AppInfo app = appInfoRepository.findByAppId(appId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在"));
        UserAiCredential current = repository.findByUserIdAndAppIdAndProviderCode(user.getId(), app.getAppId(), providerCode).orElse(null);
        if (current == null) {
            return repository.save(new UserAiCredential(user.getId(), app.getAppId(), providerCode, apiKey, quotaUnits));
        }
        current.update(providerCode, apiKey, quotaUnits);
        return repository.save(current);
    }
}
