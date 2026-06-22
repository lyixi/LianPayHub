package com.lianpayhub.repository;

import com.lianpayhub.domain.product.ProductPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductPlanRepository extends JpaRepository<ProductPlan, Long> {
    List<ProductPlan> findByProductIdOrderBySortOrderAscIdAsc(Long productId);
    List<ProductPlan> findByAppIdOrderBySortOrderAscIdAsc(String appId);
    boolean existsByProductIdAndPlanCode(Long productId, String planCode);
    Optional<ProductPlan> findByProductIdAndPlanCode(Long productId, String planCode);
}
