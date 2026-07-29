package com.lianpayhub.service.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.member.MemberInfo;
import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.domain.payment.PaymentOrder;
import com.lianpayhub.domain.platform.AppPlatformPolicy;
import com.lianpayhub.domain.platform.PlatformConfigCategory;
import com.lianpayhub.repository.DeviceInfoRepository;
import com.lianpayhub.repository.MemberInfoRepository;
import com.lianpayhub.repository.PackageInfoRepository;
import com.lianpayhub.repository.PaymentOrderRepository;
import com.lianpayhub.service.app.AppService;
import com.lianpayhub.service.platform.AppPlatformPolicyService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnifiedSearchService {

    private final AppService appService;
    private final AppPlatformPolicyService appPlatformPolicyService;
    private final PackageInfoRepository packageInfoRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final MemberInfoRepository memberInfoRepository;

    public UnifiedSearchService(AppService appService,
                                AppPlatformPolicyService appPlatformPolicyService,
                                PackageInfoRepository packageInfoRepository,
                                PaymentOrderRepository paymentOrderRepository,
                                DeviceInfoRepository deviceInfoRepository,
                                MemberInfoRepository memberInfoRepository) {
        this.appService = appService;
        this.appPlatformPolicyService = appPlatformPolicyService;
        this.packageInfoRepository = packageInfoRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.memberInfoRepository = memberInfoRepository;
    }

    @Transactional(readOnly = true)
    public List<UnifiedSearchResult> search(String appId, String keyword, int limit) {
        String safeAppId = requireText(appId, "appId 不能为空");
        String safeKeyword = requireText(keyword, "keyword 不能为空");
        appService.requireEnabledApp(safeAppId);

        AppPlatformPolicy policy = appPlatformPolicyService.find(safeAppId, PlatformConfigCategory.SEARCH).orElse(null);
        if (policy != null && !policy.isEnabled()) {
            throw new BusinessException(ErrorCode.CONFLICT, "APP 搜索策略已停用");
        }
        JsonNode policyJson = appPlatformPolicyService.policyJson(policy);
        int maxLimit = clamp(appPlatformPolicyService.intValue(policyJson, "maxLimit", 50), 1, 100);
        int safeLimit = clamp(limit <= 0 ? 20 : limit, 1, maxLimit);

        List<UnifiedSearchResult> results = new ArrayList<UnifiedSearchResult>();
        collectPackages(safeAppId, safeKeyword, safeLimit, results);
        collectOrders(safeAppId, safeKeyword, safeLimit, results);
        collectDevices(safeAppId, safeKeyword, safeLimit, results);
        collectMembers(safeAppId, safeKeyword, safeLimit, results);
        if (results.size() > safeLimit) {
            return new ArrayList<UnifiedSearchResult>(results.subList(0, safeLimit));
        }
        return results;
    }

    private void collectPackages(String appId, String keyword, int limit, List<UnifiedSearchResult> results) {
        String lower = keyword.toLowerCase(Locale.ROOT);
        List<PackageInfo> packages = packageInfoRepository.findByAppId(appId);
        for (PackageInfo item : packages) {
            if (results.size() >= limit) return;
            if (contains(item.getPackageName(), lower) || contains(item.getBenefitsText(), lower)
                    || contains(String.valueOf(item.getPackageType()), lower)) {
                results.add(new UnifiedSearchResult("PACKAGE", item.getId(), item.getAppId(),
                        item.getPackageName(), item.getBenefitsText())
                        .attr("priceCents", item.getPriceCents())
                        .attr("durationDays", item.getDurationDays())
                        .attr("status", item.getStatus()));
            }
        }
    }

    private void collectOrders(String appId, String keyword, int limit, List<UnifiedSearchResult> results) {
        List<PaymentOrder> orders = paymentOrderRepository.search(appId, keyword, PageRequest.of(0, limit)).getContent();
        for (PaymentOrder item : orders) {
            if (results.size() >= limit) return;
            results.add(new UnifiedSearchResult("ORDER", item.getId(), item.getAppId(),
                    item.getOrderNo(), String.valueOf(item.getPayStatus()))
                    .attr("amountCents", item.getAmountCents())
                    .attr("payChannel", item.getPayChannel())
                    .attr("payProvider", item.getPayProvider())
                    .attr("tradeNo", item.getTradeNo()));
        }
    }

    private void collectDevices(String appId, String keyword, int limit, List<UnifiedSearchResult> results) {
        List<DeviceInfo> devices = deviceInfoRepository
                .findByAppIdAndDeviceCode(appId, keyword, PageRequest.of(0, limit)).getContent();
        for (DeviceInfo item : devices) {
            if (results.size() >= limit) return;
            results.add(new UnifiedSearchResult("DEVICE", item.getId(), item.getAppId(),
                    item.getDeviceCode(), item.getDeviceName())
                    .attr("deviceType", item.getDeviceType())
                    .attr("bindStatus", item.getBindStatus())
                    .attr("userId", item.getUserId()));
        }
    }

    private void collectMembers(String appId, String keyword, int limit, List<UnifiedSearchResult> results) {
        Long numeric = parseLong(keyword);
        if (numeric == null) {
            return;
        }
        List<MemberInfo> members = memberInfoRepository.findByAppId(appId, PageRequest.of(0, limit)).getContent();
        for (MemberInfo item : members) {
            if (results.size() >= limit) return;
            if (numeric.equals(item.getId()) || numeric.equals(item.getUserId()) || numeric.equals(item.getDeviceId())
                    || numeric.equals(item.getOrderId()) || numeric.equals(item.getPackageId())) {
                results.add(new UnifiedSearchResult("MEMBER", item.getId(), item.getAppId(),
                        item.getMemberSubjectType() + "#" + item.getId(), String.valueOf(item.getStatus()))
                        .attr("userId", item.getUserId())
                        .attr("deviceId", item.getDeviceId())
                        .attr("packageId", item.getPackageId())
                        .attr("expireAt", item.getExpireAt()));
            }
        }
    }

    private boolean contains(String value, String lowerKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return value.trim();
    }
}
