package com.lianpayhub.service.admin;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.payment.PayStatus;
import com.lianpayhub.domain.user.UserInfo;
import com.lianpayhub.repository.*;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AdminUserProfileService {

    private final UserInfoRepository userInfoRepository;
    private final UserAppBindingRepository userAppBindingRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final AppLoginLogRepository appLoginLogRepository;
    private final LaunchRecordRepository launchRecordRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final UserFileRepository userFileRepository;

    public AdminUserProfileService(UserInfoRepository userInfoRepository,
                                   UserAppBindingRepository userAppBindingRepository,
                                   DeviceInfoRepository deviceInfoRepository,
                                   AppLoginLogRepository appLoginLogRepository,
                                   LaunchRecordRepository launchRecordRepository,
                                   PaymentOrderRepository paymentOrderRepository,
                                   MemberInfoRepository memberInfoRepository,
                                   UserFileRepository userFileRepository) {
        this.userInfoRepository = userInfoRepository;
        this.userAppBindingRepository = userAppBindingRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.appLoginLogRepository = appLoginLogRepository;
        this.launchRecordRepository = launchRecordRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.memberInfoRepository = memberInfoRepository;
        this.userFileRepository = userFileRepository;
    }

    public AdminUserProfileResult profile(Long userId) {
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));

        List<com.lianpayhub.domain.user.UserAppBinding> bindings = userAppBindingRepository.search(null, userId, null, page(20)).getContent();
        List<com.lianpayhub.domain.device.DeviceInfo> devices = deviceInfoRepository.findByUserId(userId, page(10)).getContent();
        List<com.lianpayhub.domain.log.AppLoginLog> logins = appLoginLogRepository.findByUserId(userId, page(10)).getContent();
        List<com.lianpayhub.domain.launch.LaunchRecord> launches = launchRecordRepository.findByUserId(userId, page(10)).getContent();
        List<com.lianpayhub.domain.payment.PaymentOrder> orders = paymentOrderRepository.findByUserId(userId, page(10)).getContent();
        List<com.lianpayhub.domain.member.MemberInfo> members = memberInfoRepository.findByUserId(userId, page(10)).getContent();
        List<com.lianpayhub.domain.storage.UserFile> files = userFileRepository.findByUserIdAndDeletedAtIsNull(userId, page(10)).getContent();

        long bindingCount = userAppBindingRepository.countByUserId(userId);
        long deviceCount = deviceInfoRepository.countByUserId(userId);
        long loginCount = appLoginLogRepository.countByUserId(userId);
        long launchCount = launchRecordRepository.countByUserId(userId);
        long orderCount = paymentOrderRepository.countByUserId(userId);
        long paidOrderCount = paymentOrderRepository.countByUserIdAndPayStatus(userId, PayStatus.PAID);
        long paidAmountCents = paymentOrderRepository.sumAmountCentsByUserIdAndPayStatus(userId, PayStatus.PAID);
        long memberCount = memberInfoRepository.countByUserId(userId);
        long fileCount = userFileRepository.countByUserIdAndDeletedAtIsNull(userId);
        long usedBytes = userFileRepository.sumSizeBytesByUserId(userId);

        return new AdminUserProfileResult(
                user,
                new AdminUserProfileResult.Stats(bindingCount, deviceCount, loginCount, launchCount,
                        orderCount, paidOrderCount, paidAmountCents, memberCount, fileCount, usedBytes),
                bindings,
                devices,
                logins,
                launches,
                orders,
                members,
                files
        );
    }

    private PageRequest page(int size) {
        return PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
    }
}
