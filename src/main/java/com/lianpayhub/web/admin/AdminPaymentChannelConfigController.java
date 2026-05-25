package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.domain.payment.PaymentChannelConfig;
import com.lianpayhub.domain.payment.PaymentChannelConfigStatus;
import com.lianpayhub.service.payment.PaymentChannelConfigService;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/payment-configs")
public class AdminPaymentChannelConfigController {

    private final PaymentChannelConfigService configService;

    public AdminPaymentChannelConfigController(PaymentChannelConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ApiResponse<Page<PaymentChannelConfig>> list(@RequestParam(required = false) String appId,
                                                        @RequestParam(required = false) PayChannel payChannel,
                                                        @RequestParam(required = false) PaymentChannelConfigStatus status,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(configService.search(appId, payChannel, status, pageRequest(page, size)));
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentChannelConfig> detail(@PathVariable Long id) {
        return ApiResponse.ok(configService.detail(id));
    }

    @PostMapping
    public ApiResponse<PaymentChannelConfig> create(@Valid @RequestBody CreatePaymentChannelConfigRequest request) {
        return ApiResponse.ok(configService.create(
                request.appId(),
                request.payChannel(),
                request.providerCode(),
                request.merchantId(),
                request.channelAppId(),
                request.notifyUrl(),
                request.configJson(),
                request.credentialJson()
        ));
    }

    @PutMapping("/{id}")
    public ApiResponse<PaymentChannelConfig> update(@PathVariable Long id,
                                                    @Valid @RequestBody UpdatePaymentChannelConfigRequest request) {
        return ApiResponse.ok(configService.update(
                id,
                request.providerCode(),
                request.merchantId(),
                request.channelAppId(),
                request.notifyUrl(),
                request.configJson(),
                request.credentialJson()
        ));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<PaymentChannelConfig> changeStatus(@PathVariable Long id,
                                                          @Valid @RequestBody ChangePaymentChannelConfigStatusRequest request) {
        return ApiResponse.ok(configService.changeStatus(id, request.status()));
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
