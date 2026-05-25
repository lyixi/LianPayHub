package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.security.AdminPrincipal;
import com.lianpayhub.service.payment.PaymentService;
import javax.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final PaymentService paymentService;

    public AdminOrderController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{id}/mark-paid")
    public ApiResponse<Void> markPaid(@PathVariable Long id,
                                      @Valid @RequestBody AdminMarkPaidRequest request,
                                      @AuthenticationPrincipal AdminPrincipal principal) {
        paymentService.markPaidByAdmin(
                id,
                request.tradeNo(),
                principal == null ? null : String.valueOf(principal.getAdminId())
        );
        return ApiResponse.ok();
    }
}
