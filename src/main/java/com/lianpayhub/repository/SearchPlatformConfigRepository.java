package com.lianpayhub.repository;

import com.lianpayhub.domain.search.SearchPlatformConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchPlatformConfigRepository extends JpaRepository<SearchPlatformConfig, Long> {
    Optional<SearchPlatformConfig> findByProviderCode(String providerCode);
}
