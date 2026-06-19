package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.domain.packageinfo.PackageStatus;
import com.lianpayhub.repository.PackageInfoRepository;
import com.lianpayhub.service.app.AppService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    private final PackageInfoRepository packageInfoRepository;
    private final AppService appService;

    public PackageController(PackageInfoRepository packageInfoRepository, AppService appService) {
        this.packageInfoRepository = packageInfoRepository;
        this.appService = appService;
    }

    @GetMapping
    public ApiResponse<List<PackageInfo>> list(@RequestParam String appId) {
        appService.requireEnabledApp(appId);
        return ApiResponse.ok(packageInfoRepository.findByAppIdAndStatus(appId, PackageStatus.ENABLED));
    }
}
