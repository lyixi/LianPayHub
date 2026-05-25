package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.service.app.AppService;
import com.lianpayhub.service.app.ChangeAppStatusCommand;
import com.lianpayhub.service.app.CreateAppCommand;
import com.lianpayhub.service.app.CreateAppResult;
import com.lianpayhub.service.app.ResetAppSecretResult;
import com.lianpayhub.service.app.UpdateAppCommand;
import javax.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/apps")
public class AdminAppController {

    private final AppService appService;
    private final AppInfoRepository appInfoRepository;

    public AdminAppController(AppService appService, AppInfoRepository appInfoRepository) {
        this.appService = appService;
        this.appInfoRepository = appInfoRepository;
    }

    @PostMapping
    public ApiResponse<CreateAppResult> create(@Valid @RequestBody CreateAppRequest request) {
        return ApiResponse.ok(appService.createApp(new CreateAppCommand(
                request.appId(),
                request.appName(),
                request.appType(),
                request.needMobileLogin(),
                request.needDeviceVip()
        )));
    }

    @GetMapping
    public ApiResponse<List<AppInfo>> list() {
        return ApiResponse.ok(appInfoRepository.findAll());
    }

    @PutMapping("/{id}")
    public ApiResponse<AppInfo> update(@PathVariable Long id, @Valid @RequestBody UpdateAppRequest request) {
        return ApiResponse.ok(appService.updateApp(id, new UpdateAppCommand(
                request.appName(),
                request.needMobileLogin(),
                request.needDeviceVip()
        )));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<AppInfo> changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeAppStatusRequest request) {
        return ApiResponse.ok(appService.changeStatus(id, new ChangeAppStatusCommand(request.status())));
    }

    @PostMapping("/{id}/reset-secret")
    public ApiResponse<ResetAppSecretResult> resetSecret(@PathVariable Long id) {
        return ApiResponse.ok(appService.resetSecret(id));
    }
}
