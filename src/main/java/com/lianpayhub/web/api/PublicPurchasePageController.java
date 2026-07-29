package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.domain.product.ProductInfo;
import com.lianpayhub.domain.product.ProductPlan;
import com.lianpayhub.domain.purchase.PurchasePageConfig;
import com.lianpayhub.repository.ProductInfoRepository;
import com.lianpayhub.repository.ProductPlanRepository;
import com.lianpayhub.repository.PurchasePageConfigRepository;
import com.lianpayhub.service.app.AppService;
import com.lianpayhub.service.device.DeviceService;
import com.lianpayhub.service.payment.CreateOrderCommand;
import com.lianpayhub.service.payment.CreateOrderResult;
import com.lianpayhub.service.payment.PaymentService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PublicPurchasePageController {
    private final PurchasePageConfigRepository purchasePageConfigRepository;
    private final ProductInfoRepository productInfoRepository;
    private final ProductPlanRepository productPlanRepository;
    private final AppService appService;
    private final PaymentService paymentService;
    private final DeviceService deviceService;

    public PublicPurchasePageController(PurchasePageConfigRepository purchasePageConfigRepository,
                                        ProductInfoRepository productInfoRepository,
                                        ProductPlanRepository productPlanRepository,
                                        AppService appService, PaymentService paymentService,
                                        DeviceService deviceService) {
        this.purchasePageConfigRepository = purchasePageConfigRepository;
        this.productInfoRepository = productInfoRepository;
        this.productPlanRepository = productPlanRepository;
        this.appService = appService;
        this.paymentService = paymentService;
        this.deviceService = deviceService;
    }

    @GetMapping("/p/{slug}")
    public String page() {
        return "forward:/purchase-ui/index.html";
    }

    @GetMapping("/public/purchase-pages/{slug}")
    @ResponseBody
    public ApiResponse<PublicPurchasePageResult> detail(@PathVariable String slug) {
        PurchasePageConfig page = purchasePageConfigRepository.findByPageSlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "购买页不存在"));
        AppInfo app = appService.requireEnabledApp(page.getAppId());
        List<ProductInfo> products = productInfoRepository.findByAppIdOrderBySortOrderAscIdAsc(page.getAppId());
        List<ProductPlan> plans = productPlanRepository.findByAppIdOrderBySortOrderAscIdAsc(page.getAppId());
        java.util.List<PayChannel> payChannels = paymentService.listEnabledPayChannels();
        return ApiResponse.ok(new PublicPurchasePageResult(page, app, products, plans, payChannels));
    }

    @PostMapping("/public/purchase-orders")
    @ResponseBody
    public ApiResponse<CreateOrderResult> createOrder(@RequestBody CreatePublicPurchaseOrderRequest request) {
        PurchasePageConfig page = purchasePageConfigRepository.findByPageSlug(request.pageSlug())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "购买页不存在"));
        ProductInfo product = resolveProduct(page.getAppId(), request);
        ProductPlan plan = resolvePlan(product, request);
        Long deviceId = request.deviceId();
        if (deviceId == null && request.deviceCode() != null && !request.deviceCode().trim().isEmpty()) {
            DeviceInfo device = deviceService.registerOrGet(new com.lianpayhub.service.device.RegisterDeviceCommand(
                    page.getAppId(), request.deviceCode().trim(), null, null, null
            ));
            deviceId = device.getId();
        }
        PayChannel payChannel = request.payChannel() == null ? PayChannel.ALIPAY : request.payChannel();
        Long packageId = plan.getLegacyPackageId();
        if (packageId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前方案尚未绑定旧套餐，暂不能真实下单");
        }
        return ApiResponse.ok(paymentService.createOrder(new CreateOrderCommand(
                page.getAppId(), request.userId(), deviceId, packageId, payChannel
        )));
    }

    private ProductInfo resolveProduct(String appId, CreatePublicPurchaseOrderRequest request) {
        if (request.productId() != null) {
            ProductInfo product = productInfoRepository.findById(request.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
            if (!appId.equals(product.getAppId())) throw new BusinessException(ErrorCode.CONFLICT, "商品不属于该购买页");
            return product;
        }
        if (request.productCode() != null && !request.productCode().trim().isEmpty()) {
            ProductInfo product = productInfoRepository.findByAppIdAndProductCode(appId, request.productCode().trim())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
            if (request.productType() != null && !request.productType().trim().isEmpty() && !request.productType().equals(product.getProductType().name())) {
                throw new BusinessException(ErrorCode.CONFLICT, "商品类型不匹配");
            }
            return product;
        }
        if (request.productType() != null && !request.productType().trim().isEmpty()) {
            return productInfoRepository.findByAppIdOrderBySortOrderAscIdAsc(appId).stream()
                    .filter(p -> request.productType().equals(p.getProductType().name()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "缺少商品信息");
    }

    private ProductPlan resolvePlan(ProductInfo product, CreatePublicPurchaseOrderRequest request) {
        if (request.planId() != null) {
            ProductPlan plan = productPlanRepository.findById(request.planId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "方案不存在"));
            if (!product.getId().equals(plan.getProductId())) throw new BusinessException(ErrorCode.CONFLICT, "方案不属于该商品");
            return plan;
        }
        if (request.planCode() != null && !request.planCode().trim().isEmpty()) {
            return productPlanRepository.findByProductIdAndPlanCode(product.getId(), request.planCode().trim())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "方案不存在"));
        }
        return productPlanRepository.findByProductIdOrderBySortOrderAscIdAsc(product.getId()).stream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "当前商品没有可购买方案"));
    }
}
