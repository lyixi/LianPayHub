package com.lianpayhub.repository;

import com.lianpayhub.domain.website.WebsiteMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WebsiteMediaRepository extends JpaRepository<WebsiteMedia,Long> {
    List<WebsiteMedia> findAllByOrderByCreatedAtDesc();
}
