package com.lianpayhub.repository;

import com.lianpayhub.domain.admin.AdminUser;
import com.lianpayhub.domain.admin.AdminUserStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUsername(String username);
    Page<AdminUser> findByUsernameContaining(String username, Pageable pageable);
    boolean existsByUsername(String username);
    long countByStatus(AdminUserStatus status);
}
