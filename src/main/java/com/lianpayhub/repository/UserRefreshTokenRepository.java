package com.lianpayhub.repository;

import com.lianpayhub.domain.auth.UserRefreshToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserRefreshToken> findByTokenHash(String tokenHash);
    List<UserRefreshToken> findByUserIdAndRevokedAtIsNull(Long userId);
    List<UserRefreshToken> findByUserIdAndAppIdAndRevokedAtIsNull(Long userId, String appId);
    List<UserRefreshToken> findByUserIdAndAppIdAndDeviceCodeAndRevokedAtIsNull(Long userId, String appId, String deviceCode);
    List<UserRefreshToken> findByAppIdAndDeviceCodeAndRevokedAtIsNull(String appId, String deviceCode);
    long deleteByExpiresAtBeforeOrRevokedAtBefore(LocalDateTime expiresBefore, LocalDateTime revokedBefore);
}
