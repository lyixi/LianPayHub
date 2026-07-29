package com.lianpayhub.service.search;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.search.SearchPlatformConfig;
import com.lianpayhub.repository.SearchPlatformConfigRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchPlatformAdminService {
    private static final List<String> SUPPORTED_PROVIDER_CODES = Arrays.asList("bocha");

    private final SearchPlatformConfigRepository repository;

    public SearchPlatformAdminService(SearchPlatformConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SearchPlatformConfig> list() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public SearchPlatformConfig detail(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "搜索平台不存在"));
    }

    @Transactional
    public SearchPlatformConfig create(String providerCode, String displayName, String baseUrl,
                                       String consoleBaseUrl, String configJson, String credentialJson) {
        String safeProviderCode = normalizeProviderCode(providerCode);
        if (!SUPPORTED_PROVIDER_CODES.contains(safeProviderCode)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "搜索平台适配器不存在: " + providerCode);
        }
        if (repository.findByProviderCode(safeProviderCode).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT, "搜索平台编码已存在");
        }
        return repository.save(new SearchPlatformConfig(safeProviderCode, displayName, baseUrl, consoleBaseUrl,
                configJson, credentialJson));
    }

    @Transactional
    public SearchPlatformConfig update(Long id, String displayName, String baseUrl, String consoleBaseUrl,
                                       String configJson, String credentialJson) {
        SearchPlatformConfig config = detail(id);
        config.update(displayName, baseUrl, consoleBaseUrl, configJson, credentialJson);
        return repository.save(config);
    }

    @Transactional
    public SearchPlatformConfig changeEnabled(Long id, boolean enabled) {
        SearchPlatformConfig config = detail(id);
        config.changeEnabled(enabled);
        return repository.save(config);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(detail(id));
    }

    private String normalizeProviderCode(String providerCode) {
        if (providerCode == null || providerCode.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "搜索平台编码不能为空");
        }
        return providerCode.trim().toLowerCase(Locale.ROOT);
    }
}
