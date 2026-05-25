package com.lianpayhub.repository;

import com.lianpayhub.domain.user.UserInfo;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByMobile(String mobile);
    Page<UserInfo> findByMobile(String mobile, Pageable pageable);
}
