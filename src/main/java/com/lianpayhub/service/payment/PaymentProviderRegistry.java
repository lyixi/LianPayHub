package com.lianpayhub.service.payment;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.payment.PayChannel;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PaymentProviderRegistry {

    private final List<PaymentProvider> providers;

    public PaymentProviderRegistry(List<PaymentProvider> providers) {
        this.providers = providers;
    }

    public PaymentProvider require(PayChannel channel) {
        return providers.stream()
                .filter(provider -> provider.payChannel() == channel)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "支付渠道暂未配置"));
    }
}
