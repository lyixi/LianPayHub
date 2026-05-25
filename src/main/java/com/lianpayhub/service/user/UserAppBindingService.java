package com.lianpayhub.service.user;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.user.BindType;
import com.lianpayhub.domain.user.BindingStatus;
import com.lianpayhub.domain.user.UserAppBinding;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.UserAppBindingRepository;
import com.lianpayhub.repository.UserInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAppBindingService {

    private final UserAppBindingRepository bindingRepository;
    private final UserInfoRepository userInfoRepository;
    private final AppInfoRepository appInfoRepository;

    public UserAppBindingService(UserAppBindingRepository bindingRepository,
                                 UserInfoRepository userInfoRepository,
                                 AppInfoRepository appInfoRepository) {
        this.bindingRepository = bindingRepository;
        this.userInfoRepository = userInfoRepository;
        this.appInfoRepository = appInfoRepository;
    }

    public Page<UserAppBinding> search(String appId, Long userId, BindingStatus status, Pageable pageable) {
        return bindingRepository.search(normalize(appId), userId, status, pageable);
    }

    public UserAppBinding detail(Long id) {
        return bindingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "绑定关系不存在"));
    }

    @Transactional
    public UserAppBinding create(Long userId, String appId, BindType bindType) {
        String normalizedAppId = normalize(appId);
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "userId 不能为空");
        }
        if (normalizedAppId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "appId 不能为空");
        }
        userInfoRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        if (!appInfoRepository.existsByAppId(normalizedAppId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在");
        }
        if (bindingRepository.existsByUserIdAndAppId(userId, normalizedAppId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户已绑定该 APP");
        }
        BindType safeBindType = bindType == null ? BindType.MOBILE_LOGIN : bindType;
        return bindingRepository.save(new UserAppBinding(userId, normalizedAppId, safeBindType));
    }

    @Transactional
    public UserAppBinding changeStatus(Long id, BindingStatus status) {
        if (status == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "status 不能为空");
        }
        UserAppBinding binding = detail(id);
        binding.changeStatus(status);
        return bindingRepository.save(binding);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
