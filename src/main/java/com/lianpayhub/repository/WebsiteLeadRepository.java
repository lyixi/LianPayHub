package com.lianpayhub.repository;
import com.lianpayhub.domain.website.WebsiteLead;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WebsiteLeadRepository extends JpaRepository<WebsiteLead,Long>{ List<WebsiteLead> findAllByOrderByCreatedAtDesc(); }
