package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.product.*;
import com.lianpayhub.service.product.ProductAdminService;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {
    private final ProductAdminService service;

    public AdminProductController(ProductAdminService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<ProductInfo>> list(@RequestParam String appId) {
        return ApiResponse.ok(service.list(appId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductAdminService.ProductDetailResult> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.detail(id));
    }

    @PostMapping
    public ApiResponse<ProductInfo> create(@Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.ok(service.create(request.appId, request.productCode, request.productName,
                request.productType, request.fulfillmentType, request.description, request.benefitsText, request.sortOrder));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductInfo> update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return ApiResponse.ok(service.update(id, request.productName, request.description, request.benefitsText, request.sortOrder));
    }

    @PostMapping("/{id}/plans")
    public ApiResponse<ProductPlan> createPlan(@PathVariable Long id, @Valid @RequestBody CreatePlanRequest request) {
        return ApiResponse.ok(service.createPlan(id, request.planCode, request.planName, request.priceCents,
                request.originalPriceCents, request.durationDays, request.creditAmount, request.badgeText,
                request.benefitsText, request.sortOrder, request.legacyPackageId));
    }

    @PutMapping("/plans/{id}")
    public ApiResponse<ProductPlan> updatePlan(@PathVariable Long id, @Valid @RequestBody UpdatePlanRequest request) {
        return ApiResponse.ok(service.updatePlan(id, request.planName, request.priceCents, request.originalPriceCents,
                request.durationDays, request.creditAmount, request.badgeText, request.benefitsText, request.sortOrder));
    }

    public static class CreateProductRequest {
        @NotBlank public String appId;
        @NotBlank public String productCode;
        @NotBlank public String productName;
        @NotNull public ProductType productType;
        @NotNull public FulfillmentType fulfillmentType;
        public String description;
        public String benefitsText;
        public Integer sortOrder;
    }

    public static class CreatePlanRequest {
        @NotBlank public String planCode;
        @NotBlank public String planName;
        @NotNull public Integer priceCents;
        public Integer originalPriceCents;
        public Integer durationDays;
        public Integer creditAmount;
        public String badgeText;
        public String benefitsText;
        public Integer sortOrder;
        public Long legacyPackageId;
    }

    public static class UpdateProductRequest {
        @NotBlank public String productName;
        public String description;
        public String benefitsText;
        public Integer sortOrder;
    }

    public static class UpdatePlanRequest {
        @NotBlank public String planName;
        @NotNull public Integer priceCents;
        public Integer originalPriceCents;
        public Integer durationDays;
        public Integer creditAmount;
        public String badgeText;
        public String benefitsText;
        public Integer sortOrder;
    }
}
