package com.lianpayhub.repository;

import com.lianpayhub.domain.purchase.PurchasePageConfig;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchasePageConfigRepository extends JpaRepository<PurchasePageConfig, Long> {
    List<PurchasePageConfig> findByAppIdOrderByIdDesc(String appId);
    Optional<PurchasePageConfig> findByPageSlug(String pageSlug);
    boolean existsByPageSlug(String pageSlug);
}
