package com.lianpayhub.repository;

import com.lianpayhub.domain.ai.UserAiCredential;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAiCredentialRepository extends JpaRepository<UserAiCredential, Long> {
    Optional<UserAiCredential> findByUserIdAndAppIdAndProviderCode(Long userId, String appId, String providerCode);
    Page<UserAiCredential> findByAppId(String appId, Pageable pageable);
    java.util.List<UserAiCredential> findByUserIdOrderByIdDesc(Long userId);
}
