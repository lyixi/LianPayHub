package com.lianpayhub.repository;

import com.lianpayhub.domain.member.MemberInfo;
import com.lianpayhub.domain.member.MemberSubjectType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {
    Optional<MemberInfo> findFirstByAppIdAndMemberSubjectTypeAndUserIdOrderByExpireAtDesc(
            String appId, MemberSubjectType subjectType, Long userId);

    Optional<MemberInfo> findFirstByAppIdAndMemberSubjectTypeAndDeviceIdOrderByExpireAtDesc(
            String appId, MemberSubjectType subjectType, Long deviceId);

    Page<MemberInfo> findByAppId(String appId, Pageable pageable);

    Optional<MemberInfo> findByOrderId(Long orderId);
}
