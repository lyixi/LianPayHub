package com.lianpayhub.service.config;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.config.UserConfig;
import com.lianpayhub.repository.UserConfigRepository;
import com.lianpayhub.repository.UserConfigVersionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserConfigService {

    private static final long MAX_CONFIG_BYTES = 512 * 1024;

    private final UserConfigRepository userConfigRepository;
    private final UserConfigVersionRepository userConfigVersionRepository;

    public UserConfigService(UserConfigRepository userConfigRepository,
                             UserConfigVersionRepository userConfigVersionRepository) {
        this.userConfigRepository = userConfigRepository;
        this.userConfigVersionRepository = userConfigVersionRepository;
    }

    @Transactional(readOnly = true)
    public List<UserConfigResult> list(Long userId, String appId) {
        return userConfigRepository.findByUserIdAndAppIdOrderByConfigKeyAsc(userId, appId)
                .stream()
                .filter(config -> !config.isDeleted())
                .map(UserConfigResult::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserConfigResult> changes(Long userId, String appId, Long sinceVersion) {
        return userConfigRepository.findByUserIdAndAppIdAndVersionGreaterThanOrderByVersionAsc(
                        userId, appId, sinceVersion == null ? 0L : sinceVersion)
                .stream().map(UserConfigResult::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserConfigResult get(Long userId, String appId, String key) {
        UserConfig config = requireConfig(userId, appId, normalizeKey(key));
        if (config.isDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "配置不存在");
        }
        return new UserConfigResult(config);
    }

    @Transactional
    public UserConfigResult put(Long userId, String appId, String key, String contentType, String contentText) {
        String normalizedKey = normalizeKey(key);
        String text = contentText == null ? "" : contentText;
        long size = text.getBytes(StandardCharsets.UTF_8).length;
        if (size > MAX_CONFIG_BYTES) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "单条配置不能超过 512 KB");
        }
        String hash = sha256(text);
        long version = nextVersion(userId, appId);
        UserConfig config = userConfigRepository.findByUserIdAndAppIdAndConfigKey(userId, appId, normalizedKey)
                .orElseGet(() -> new UserConfig(userId, appId, normalizedKey, contentType, text, hash, size));
        if (config.getId() != null) {
            config.update(contentType, text, hash, size);
        }
        config.overrideVersion(version);
        return new UserConfigResult(userConfigRepository.save(config));
    }

    @Transactional
    public void delete(Long userId, String appId, String key) {
        String normalizedKey = normalizeKey(key);
        UserConfig config = userConfigRepository.findByUserIdAndAppIdAndConfigKey(userId, appId, normalizedKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "配置不存在"));
        config.markDeleted();
        config.overrideVersion(nextVersion(userId, appId));
        userConfigRepository.save(config);
    }

    private UserConfig requireConfig(Long userId, String appId, String key) {
        return userConfigRepository.findByUserIdAndAppIdAndConfigKey(userId, appId, key)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "配置不存在"));
    }

    private String normalizeKey(String key) {
        if (key == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "配置 key 不能为空");
        }
        String value = key.trim().toLowerCase(Locale.ROOT);
        if (!value.matches("^[a-z0-9][a-z0-9_.:/-]{0,127}$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "配置 key 格式不正确");
        }
        return value;
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "配置摘要计算失败");
        }
    }

    private long nextVersion(Long userId, String appId) {
        return userConfigVersionRepository.lockByUserIdAndAppId(userId, appId)
                .orElseGet(() -> userConfigVersionRepository.save(new com.lianpayhub.domain.config.UserConfigVersion(userId, appId)))
                .nextVersion();
    }
}
