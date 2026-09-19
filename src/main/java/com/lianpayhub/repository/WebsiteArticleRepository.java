package com.lianpayhub.repository;
import com.lianpayhub.domain.website.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WebsiteArticleRepository extends JpaRepository<WebsiteArticle,Long>{ Optional<WebsiteArticle> findBySlug(String slug); boolean existsBySlug(String slug); List<WebsiteArticle> findByStatusOrderByPublishedAtDesc(ContentStatus status); List<WebsiteArticle> findByCategoryAndStatusOrderByPublishedAtDesc(String category,ContentStatus status); List<WebsiteArticle> findAllByOrderByUpdatedAtDesc(); }
