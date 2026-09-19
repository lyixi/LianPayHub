package com.lianpayhub.repository;
import com.lianpayhub.domain.website.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WebsiteProductRepository extends JpaRepository<WebsiteProduct,Long>{ Optional<WebsiteProduct> findBySlug(String slug); boolean existsBySlug(String slug); List<WebsiteProduct> findByStatusOrderBySortOrderAscIdAsc(ContentStatus status); List<WebsiteProduct> findAllByOrderBySortOrderAscIdAsc(); }
