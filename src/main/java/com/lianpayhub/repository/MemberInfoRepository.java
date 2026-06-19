package com.lianpayhub.repository;

import com.lianpayhub.domain.member.MemberInfo;
import com.lianpayhub.domain.member.MemberSubjectType;
import java.util.List;
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
    List<MemberInfo> findTop10ByAppIdOrderByIdDesc(String appId);
    long countByAppId(String appId);

    Optional<MemberInfo> findByOrderId(Long orderId);
}
