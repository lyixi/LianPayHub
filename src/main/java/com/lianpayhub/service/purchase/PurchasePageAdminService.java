package com.lianpayhub.service.purchase;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.purchase.PurchaseLayoutType;
import com.lianpayhub.domain.purchase.PurchasePageConfig;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.PurchasePageConfigRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchasePageAdminService {
    private final PurchasePageConfigRepository repository;
    private final AppInfoRepository appInfoRepository;

    public PurchasePageAdminService(PurchasePageConfigRepository repository, AppInfoRepository appInfoRepository) {
        this.repository = repository;
        this.appInfoRepository = appInfoRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchasePageConfig> list(String appId) {
        return repository.findByAppIdOrderByIdDesc(appId);
    }

    @Transactional(readOnly = true)
    public PurchasePageConfig detail(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "购买页不存在"));
    }

    @Transactional
    public PurchasePageConfig create(String appId, String pageSlug, String title, String subtitle,
                                     PurchaseLayoutType layoutType, String themeJson, String contentJson) {
        if (!appInfoRepository.existsByAppId(appId)) throw new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在");
        if (repository.existsByPageSlug(pageSlug)) throw new BusinessException(ErrorCode.CONFLICT, "页面标识已存在");
        return repository.save(new PurchasePageConfig(appId, pageSlug, title, subtitle, layoutType, themeJson, contentJson));
    }
}
