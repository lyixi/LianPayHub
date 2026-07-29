package com.lianpayhub.repository;

import com.lianpayhub.domain.user.UserAppBinding;
import com.lianpayhub.domain.user.BindingStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAppBindingRepository extends JpaRepository<UserAppBinding, Long> {
    boolean existsByUserIdAndAppId(Long userId, String appId);
    Optional<UserAppBinding> findByUserIdAndAppId(Long userId, String appId);
    long countByAppId(String appId);
    long countByUserId(Long userId);
    void deleteByAppId(String appId);

    @Query("select b from UserAppBinding b where (:appId is null or b.appId = :appId) " +
            "and (:userId is null or b.userId = :userId) " +
            "and (:status is null or b.status = :status)")
    Page<UserAppBinding> search(@Param("appId") String appId,
                                @Param("userId") Long userId,
                                @Param("status") BindingStatus status,
                                Pageable pageable);
}
