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
import com.lianpayhub.service.admin.AdminAggregateService;
import com.lianpayhub.service.admin.IntegrationPackageService;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/apps")
public class AdminAppController {

    private final AppService appService;
    private final AppInfoRepository appInfoRepository;
    private final AdminAggregateService adminAggregateService;
    private final IntegrationPackageService integrationPackageService;

    public AdminAppController(AppService appService, AppInfoRepository appInfoRepository,
                              AdminAggregateService adminAggregateService,
                              IntegrationPackageService integrationPackageService) {
        this.appService = appService;
        this.appInfoRepository = appInfoRepository;
        this.adminAggregateService = adminAggregateService;
        this.integrationPackageService = integrationPackageService;
    }

    @PostMapping
    public ApiResponse<CreateAppResult> create(@Valid @RequestBody CreateAppRequest request) {
        return ApiResponse.ok(appService.createApp(new CreateAppCommand(
                request.appId(),
                request.appName(),
                request.appType(),
                request.needMobileLogin(),
                request.needDeviceVip(),
                request.enableUserAiKey(),
                request.defaultAiQuotaUnits(),
                request.defaultAiProviderCode()
        )));
    }

    @GetMapping
    public ApiResponse<List<AppInfo>> list() {
        return ApiResponse.ok(appInfoRepository.findAll());
    }

    @GetMapping("/{id}/aggregate")
    public ApiResponse<AppAggregateResult> aggregate(@PathVariable Long id) {
        return ApiResponse.ok(adminAggregateService.appAggregate(id));
    }

    @GetMapping("/{id}/integration-package")
    public ResponseEntity<String> integrationPackage(@PathVariable Long id) {
        String markdown = integrationPackageService.buildMarkdown(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("lianpayhub-integration-" + id + ".md", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(new MediaType("text", "markdown", StandardCharsets.UTF_8))
                .body(markdown);
    }

    @PutMapping("/{id}")
    public ApiResponse<AppInfo> update(@PathVariable Long id, @Valid @RequestBody UpdateAppRequest request) {
        return ApiResponse.ok(appService.updateApp(id, new UpdateAppCommand(
                request.appName(),
                request.needMobileLogin(),
                request.needDeviceVip(),
                request.enableUserAiKey(),
                request.defaultAiQuotaUnits(),
                request.defaultAiProviderCode()
        )));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<AppInfo> changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeAppStatusRequest request) {
        return ApiResponse.ok(appService.changeStatus(id, new ChangeAppStatusCommand(request.status())));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        appService.deleteApp(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/reset-secret")
    public ApiResponse<ResetAppSecretResult> resetSecret(@PathVariable Long id) {
        return ApiResponse.ok(appService.resetSecret(id));
    }
}
