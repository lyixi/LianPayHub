package com.lianpayhub.repository;

import com.lianpayhub.domain.ai.AiProviderConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiProviderConfigRepository extends JpaRepository<AiProviderConfig, Long> {
    Optional<AiProviderConfig> findByProviderCode(String providerCode);
}
