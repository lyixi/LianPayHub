package com.lianpayhub.repository;

import com.lianpayhub.domain.config.UserConfigVersion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;

public interface UserConfigVersionRepository extends JpaRepository<UserConfigVersion, Long> {
    Optional<UserConfigVersion> findByUserIdAndAppId(Long userId, String appId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from UserConfigVersion v where v.userId = :userId and v.appId = :appId")
    Optional<UserConfigVersion> lockByUserIdAndAppId(@Param("userId") Long userId, @Param("appId") String appId);
}
