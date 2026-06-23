package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.purchase.PurchaseLayoutType;
import com.lianpayhub.domain.purchase.PurchasePageConfig;
import com.lianpayhub.service.purchase.PurchasePageAdminService;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/purchase-pages")
public class AdminPurchasePageController {
    private final PurchasePageAdminService service;

    public AdminPurchasePageController(PurchasePageAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<PurchasePageConfig>> list(@RequestParam String appId) {
        return ApiResponse.ok(service.list(appId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchasePageConfig> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id));
    }

    @PostMapping
    public ApiResponse<PurchasePageConfig> create(@Valid @RequestBody CreatePageRequest request) {
        return ApiResponse.ok(service.create(request.appId, request.pageSlug, request.title, request.subtitle,
                request.layoutType, request.themeJson, request.contentJson, request.defaultProductCode,
                request.defaultPlanCode, request.defaultPayChannel));
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchasePageConfig> update(@PathVariable Long id, @Valid @RequestBody CreatePageRequest request) {
        return ApiResponse.ok(service.update(id, request.title, request.subtitle, request.layoutType,
                request.themeJson, request.contentJson, request.defaultProductCode, request.defaultPlanCode,
                request.defaultPayChannel));
    }

    public static class CreatePageRequest {
        @NotBlank public String appId;
        @NotBlank public String pageSlug;
        @NotBlank public String title;
        public String subtitle;
        @NotNull public PurchaseLayoutType layoutType;
        public String themeJson;
        public String contentJson;
        public String defaultProductCode;
        public String defaultPlanCode;
        public com.lianpayhub.domain.payment.PayChannel defaultPayChannel;
    }
}
