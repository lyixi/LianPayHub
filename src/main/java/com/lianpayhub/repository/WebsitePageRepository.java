package com.lianpayhub.repository;
import com.lianpayhub.domain.website.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WebsitePageRepository extends JpaRepository<WebsitePage,Long>{Optional<WebsitePage> findBySlug(String slug);boolean existsBySlug(String slug);List<WebsitePage> findAllByOrderBySortOrderAscIdAsc();List<WebsitePage> findByStatusOrderBySortOrderAscIdAsc(ContentStatus status);}
