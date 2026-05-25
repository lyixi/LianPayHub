package com.lianpayhub.service.member;

import com.lianpayhub.domain.member.MemberInfo;
import com.lianpayhub.domain.member.MemberStatus;
import com.lianpayhub.domain.member.MemberSubjectType;
import com.lianpayhub.repository.MemberInfoRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberInfoRepository memberInfoRepository;

    public MemberService(MemberInfoRepository memberInfoRepository) {
        this.memberInfoRepository = memberInfoRepository;
    }

    @Transactional(readOnly = true)
    public MemberStatusResult getUserMemberStatus(String appId, Long userId) {
        return toStatus(memberInfoRepository.findFirstByAppIdAndMemberSubjectTypeAndUserIdOrderByExpireAtDesc(
                appId, MemberSubjectType.USER, userId));
    }

    @Transactional(readOnly = true)
    public MemberStatusResult getDeviceMemberStatus(String appId, Long deviceId) {
        return toStatus(memberInfoRepository.findFirstByAppIdAndMemberSubjectTypeAndDeviceIdOrderByExpireAtDesc(
                appId, MemberSubjectType.DEVICE, deviceId));
    }

    @Transactional
    public MemberInfo activateOrExtend(ActivateMemberCommand command) {
        Optional<MemberInfo> current = command.subjectType() == MemberSubjectType.USER
                ? memberInfoRepository.findFirstByAppIdAndMemberSubjectTypeAndUserIdOrderByExpireAtDesc(
                        command.appId(), MemberSubjectType.USER, command.userId())
                : memberInfoRepository.findFirstByAppIdAndMemberSubjectTypeAndDeviceIdOrderByExpireAtDesc(
                        command.appId(), MemberSubjectType.DEVICE, command.deviceId());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startAt = current.map(MemberInfo::getExpireAt)
                .filter(expireAt -> expireAt.isAfter(now))
                .orElse(now);
        LocalDateTime expireAt = startAt.plusDays(command.durationDays());

        if (current.isPresent()) {
            MemberInfo memberInfo = current.get();
            memberInfo.extendTo(expireAt, command.packageId(), command.orderId());
            return memberInfo;
        }

        return memberInfoRepository.save(new MemberInfo(
                command.appId(),
                command.subjectType(),
                command.userId(),
                command.deviceId(),
                command.packageId(),
                now,
                expireAt,
                command.orderId()
        ));
    }

    private MemberStatusResult toStatus(Optional<MemberInfo> memberInfo) {
        if (!memberInfo.isPresent()) {
            return MemberStatusResult.inactive();
        }
        MemberInfo member = memberInfo.get();
        boolean active = member.getStatus() == MemberStatus.ACTIVE && member.getExpireAt().isAfter(LocalDateTime.now());
        return new MemberStatusResult(active, member.getStatus(), member.getExpireAt());
    }

    @Transactional
    public void handleRefund(String appId, Long userId, Long deviceId, Long orderId) {
        memberInfoRepository.findByOrderId(orderId).ifPresent(MemberInfo::cancel);
    }

    @Transactional
    public MemberInfo grant(GrantMemberCommand command) {
        return activateOrExtend(new ActivateMemberCommand(
                command.appId(),
                command.subjectType(),
                command.userId(),
                command.deviceId(),
                command.packageId(),
                command.durationDays(),
                0L
        ));
    }

    @Transactional
    public MemberInfo cancel(Long memberId) {
        MemberInfo memberInfo = memberInfoRepository.findById(memberId)
                .orElseThrow(() -> new com.lianpayhub.common.error.BusinessException(
                        com.lianpayhub.common.error.ErrorCode.NOT_FOUND, "会员记录不存在"));
        memberInfo.cancel();
        return memberInfo;
    }
}
