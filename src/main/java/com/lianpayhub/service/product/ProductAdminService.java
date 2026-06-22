package com.lianpayhub.service.product;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.product.*;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.ProductInfoRepository;
import com.lianpayhub.repository.ProductPlanRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductAdminService {
    private final ProductInfoRepository productInfoRepository;
    private final ProductPlanRepository productPlanRepository;
    private final AppInfoRepository appInfoRepository;

    public ProductAdminService(ProductInfoRepository productInfoRepository, ProductPlanRepository productPlanRepository,
                               AppInfoRepository appInfoRepository) {
        this.productInfoRepository = productInfoRepository;
        this.productPlanRepository = productPlanRepository;
        this.appInfoRepository = appInfoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductInfo> list(String appId) {
        return productInfoRepository.findByAppIdOrderBySortOrderAscIdAsc(appId);
    }

    @Transactional(readOnly = true)
    public ProductDetailResult detail(Long id) {
        ProductInfo product = requireProduct(id);
        return new ProductDetailResult(product, productPlanRepository.findByProductIdOrderBySortOrderAscIdAsc(id));
    }

    @Transactional
    public ProductInfo create(String appId, String productCode, String productName, ProductType productType,
                              FulfillmentType fulfillmentType, String description, String benefitsText, Integer sortOrder) {
        if (!appInfoRepository.existsByAppId(appId)) throw new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在");
        if (productInfoRepository.existsByAppIdAndProductCode(appId, productCode)) {
            throw new BusinessException(ErrorCode.CONFLICT, "商品编码已存在");
        }
        return productInfoRepository.save(new ProductInfo(appId, productCode, productName, productType,
                fulfillmentType, description, benefitsText, sortOrder));
    }

    @Transactional
    public ProductInfo update(Long id, String productName, String description, String benefitsText, Integer sortOrder) {
        ProductInfo product = requireProduct(id);
        product.update(productName, description, benefitsText, sortOrder);
        return productInfoRepository.save(product);
    }

    @Transactional
    public ProductPlan createPlan(Long productId, String planCode, String planName, Integer priceCents,
                                  Integer originalPriceCents, Integer durationDays, Integer creditAmount,
                                  String badgeText, String benefitsText, Integer sortOrder, Long legacyPackageId) {
        ProductInfo product = requireProduct(productId);
        if (productPlanRepository.existsByProductIdAndPlanCode(productId, planCode)) {
            throw new BusinessException(ErrorCode.CONFLICT, "方案编码已存在");
        }
        validatePlan(product.getProductType(), durationDays, creditAmount);
        return productPlanRepository.save(new ProductPlan(productId, product.getAppId(), planCode, planName,
                priceCents, originalPriceCents, durationDays, creditAmount, badgeText, benefitsText, sortOrder, legacyPackageId));
    }

    @Transactional
    public ProductPlan updatePlan(Long planId, String planName, Integer priceCents, Integer originalPriceCents,
                                  Integer durationDays, Integer creditAmount, String badgeText, String benefitsText, Integer sortOrder) {
        ProductPlan plan = productPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "方案不存在"));
        ProductInfo product = requireProduct(plan.getProductId());
        validatePlan(product.getProductType(), durationDays, creditAmount);
        plan.update(planName, priceCents, originalPriceCents, durationDays, creditAmount, badgeText, benefitsText, sortOrder);
        return productPlanRepository.save(plan);
    }

    private void validatePlan(ProductType type, Integer durationDays, Integer creditAmount) {
        if (type == ProductType.VIP && (durationDays == null || durationDays <= 0)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "VIP 方案必须配置 durationDays");
        }
        if (type == ProductType.AI_CREDITS && (creditAmount == null || creditAmount <= 0)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "AI 算力方案必须配置 creditAmount");
        }
    }

    private ProductInfo requireProduct(Long id) {
        return productInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
    }

    public static class ProductDetailResult {
        private final ProductInfo product;
        private final List<ProductPlan> plans;

        public ProductDetailResult(ProductInfo product, List<ProductPlan> plans) {
            this.product = product;
            this.plans = plans;
        }

        public ProductInfo getProduct() { return product; }
        public List<ProductPlan> getPlans() { return plans; }
    }
}
