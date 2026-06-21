package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.payment.PaymentRefund;
import com.lianpayhub.repository.PaymentRefundRepository;
import com.lianpayhub.security.AdminPrincipal;
import com.lianpayhub.service.payment.PaymentService;
import com.lianpayhub.service.payment.RefundCommand;
import com.lianpayhub.service.payment.RefundResult;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/refunds")
public class AdminRefundController {

    private final PaymentService paymentService;
    private final PaymentRefundRepository refundRepository;

    public AdminRefundController(PaymentService paymentService, PaymentRefundRepository refundRepository) {
        this.paymentService = paymentService;
        this.refundRepository = refundRepository;
    }

    @PostMapping
    public ApiResponse<RefundResult> create(@Valid @RequestBody CreateRefundRequest request,
                                            @AuthenticationPrincipal AdminPrincipal principal) {
        return ApiResponse.ok(paymentService.requestRefund(new RefundCommand(
                request.orderId(),
                request.amountCents(),
                request.reason(),
                principal == null ? null : String.valueOf(principal.getAdminId())
        )));
    }

    @PostMapping("/{id}/channel-refund")
    public ApiResponse<RefundResult> channelRefund(@PathVariable Long id,
                                                   @AuthenticationPrincipal AdminPrincipal principal) {
        return ApiResponse.ok(paymentService.refundToChannel(
                id,
                principal == null ? null : String.valueOf(principal.getAdminId())
        ));
    }

    @PostMapping("/{id}/mark-success")
    public ApiResponse<RefundResult> markSuccess(@PathVariable Long id,
                                                 @RequestBody(required = false) MarkRefundSuccessRequest request,
                                                 @AuthenticationPrincipal AdminPrincipal principal) {
        String channelRefundNo = request == null ? null : request.channelRefundNo();
        return ApiResponse.ok(paymentService.markRefundSuccess(
                id,
                channelRefundNo,
                principal == null ? null : String.valueOf(principal.getAdminId())
        ));
    }

    @PostMapping("/{id}/mark-failed")
    public ApiResponse<RefundResult> markFailed(@PathVariable Long id,
                                                @AuthenticationPrincipal AdminPrincipal principal) {
        return ApiResponse.ok(paymentService.markRefundFailed(
                id,
                principal == null ? null : String.valueOf(principal.getAdminId())
        ));
    }

    @GetMapping
    public ApiResponse<Page<PaymentRefund>> list(@RequestParam(required = false) String appId,
                                                 @RequestParam(required = false) Long orderId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = pageRequest(page, size);
        Page<PaymentRefund> result;
        if (orderId != null) {
            result = refundRepository.findByOrderId(orderId, pageRequest);
        } else if (appId != null && !appId.trim().isEmpty()) {
            result = refundRepository.findByAppId(appId, pageRequest);
        } else {
            result = refundRepository.findAll(pageRequest);
        }
        return ApiResponse.ok(result);
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
