package com.lianpayhub.web.api;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.config.PaymentProperties;
import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.security.AppUserPrincipal;
import com.lianpayhub.service.security.AppUserAccessService;
import com.lianpayhub.service.rate.RateLimitService;
import com.lianpayhub.service.payment.CreateOrderCommand;
import com.lianpayhub.service.payment.CreateOrderResult;
import com.lianpayhub.service.payment.PaymentNotifyResult;
import com.lianpayhub.service.payment.PaymentService;
import javax.validation.Valid;
import java.time.Duration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final RateLimitService rateLimitService;
    private final PaymentProperties paymentProperties;
    private final AppUserAccessService appUserAccessService;

    public PaymentController(PaymentService paymentService, RateLimitService rateLimitService,
                             PaymentProperties paymentProperties, AppUserAccessService appUserAccessService) {
        this.paymentService = paymentService;
        this.rateLimitService = rateLimitService;
        this.paymentProperties = paymentProperties;
        this.appUserAccessService = appUserAccessService;
    }

    @PostMapping("/create-order")
    public ApiResponse<CreateOrderResult> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                      @AuthenticationPrincipal AppUserPrincipal principal) {
        String subject = request.deviceId() == null ? "user:" + request.userId() : "device:" + request.deviceId();
        rateLimitService.requireWithinLimit("payment:create:" + request.appId() + ":" + subject,
                20, Duration.ofMinutes(1));
        appUserAccessService.requireUserAccessWhenNeeded(request.appId(), request.userId(), principal);
        return ApiResponse.ok(paymentService.createOrder(new CreateOrderCommand(
                request.appId(),
                request.userId(),
                request.deviceId(),
                request.packageId(),
                request.payChannel()
        )));
    }

    @PostMapping("/dev/mark-paid")
    public ApiResponse<Void> markPaid(@Valid @RequestBody MarkPaidRequest request) {
        if (!Boolean.TRUE.equals(paymentProperties.getDevToolsEnabled())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "调试支付入口已关闭");
        }
        paymentService.markPaidForTest(request.orderNo(), request.tradeNo());
        return ApiResponse.ok();
    }

    @PostMapping("/notify/{payChannel}")
    public ApiResponse<PaymentNotifyResult> notify(@PathVariable PayChannel payChannel,
                                                   @Valid @RequestBody PaymentNotifyRequest request) {
        return ApiResponse.ok(paymentService.handlePaymentNotify(payChannel, request.payload()));
    }
}
