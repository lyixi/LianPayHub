package com.lianpayhub.repository;

import com.lianpayhub.domain.product.ProductInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductInfoRepository extends JpaRepository<ProductInfo, Long> {
    List<ProductInfo> findByAppIdOrderBySortOrderAscIdAsc(String appId);
    Optional<ProductInfo> findByAppIdAndProductCode(String appId, String productCode);
    boolean existsByAppIdAndProductCode(String appId, String productCode);
}
