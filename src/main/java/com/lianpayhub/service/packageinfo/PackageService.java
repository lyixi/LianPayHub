package com.lianpayhub.service.packageinfo;

import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.repository.PackageInfoRepository;
import com.lianpayhub.service.app.AppService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PackageService {

    private final PackageInfoRepository packageInfoRepository;
    private final AppService appService;

    public PackageService(PackageInfoRepository packageInfoRepository, AppService appService) {
        this.packageInfoRepository = packageInfoRepository;
        this.appService = appService;
    }

    @Transactional
    public PackageInfo createPackage(CreatePackageCommand command) {
        appService.requireApp(command.appId());
        return packageInfoRepository.save(new PackageInfo(
                command.appId(),
                command.packageName(),
                command.packageType(),
                command.priceCents(),
                command.durationDays(),
                command.benefitsText()
        ));
    }

    @Transactional
    public PackageInfo updatePackage(Long id, UpdatePackageCommand command) {
        PackageInfo packageInfo = packageInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在"));
        packageInfo.update(command.packageName(), command.priceCents(), command.durationDays(), command.benefitsText());
        return packageInfo;
    }

    @Transactional
    public PackageInfo changeStatus(Long id, ChangePackageStatusCommand command) {
        PackageInfo packageInfo = packageInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在"));
        packageInfo.changeStatus(command.status());
        return packageInfo;
    }

    @Transactional(readOnly = true)
    public List<PackageInfo> listByApp(String appId) {
        return packageInfoRepository.findByAppId(appId);
    }
}
