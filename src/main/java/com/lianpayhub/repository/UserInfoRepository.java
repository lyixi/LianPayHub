package com.lianpayhub.repository;

import com.lianpayhub.domain.user.UserInfo;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByMobile(String mobile);
    Optional<UserInfo> findByUsername(String username);
    Page<UserInfo> findByMobile(String mobile, Pageable pageable);
    boolean existsByMobile(String mobile);
    boolean existsByUsername(String username);

    @Query("select u from UserInfo u where " +
            "(:keyword is null or u.mobile like concat('%', :keyword, '%') " +
            "or u.username like concat('%', :keyword, '%') " +
            "or u.nickname like concat('%', :keyword, '%') " +
            "or u.openId like concat('%', :keyword, '%')) " +
            "and (:mobile is null or u.mobile = :mobile) " +
            "and (:username is null or u.username = :username) " +
            "and (:status is null or u.status = :status)")
    Page<UserInfo> search(@Param("keyword") String keyword,
                          @Param("mobile") String mobile,
                          @Param("username") String username,
                          @Param("status") com.lianpayhub.domain.user.UserStatus status,
                          Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
