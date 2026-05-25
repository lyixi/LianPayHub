package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.service.packageinfo.ChangePackageStatusCommand;
import com.lianpayhub.service.packageinfo.CreatePackageCommand;
import com.lianpayhub.service.packageinfo.PackageService;
import com.lianpayhub.service.packageinfo.UpdatePackageCommand;
import javax.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/packages")
public class AdminPackageController {

    private final PackageService packageService;

    public AdminPackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @PostMapping
    public ApiResponse<PackageInfo> create(@Valid @RequestBody CreatePackageRequest request) {
        return ApiResponse.ok(packageService.createPackage(new CreatePackageCommand(
                request.appId(),
                request.packageName(),
                request.packageType(),
                request.priceCents(),
                request.durationDays(),
                request.benefitsText()
        )));
    }

    @GetMapping
    public ApiResponse<List<PackageInfo>> list(@RequestParam String appId) {
        return ApiResponse.ok(packageService.listByApp(appId));
    }

    @PutMapping("/{id}")
    public ApiResponse<PackageInfo> update(@PathVariable Long id, @Valid @RequestBody UpdatePackageRequest request) {
        return ApiResponse.ok(packageService.updatePackage(id, new UpdatePackageCommand(
                request.packageName(),
                request.priceCents(),
                request.durationDays(),
                request.benefitsText()
        )));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<PackageInfo> changeStatus(@PathVariable Long id,
                                                  @Valid @RequestBody ChangePackageStatusRequest request) {
        return ApiResponse.ok(packageService.changeStatus(id, new ChangePackageStatusCommand(request.status())));
    }
}
