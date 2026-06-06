package com.lianpayhub.repository;

import com.lianpayhub.domain.storage.UserStorageQuota;
import com.lianpayhub.domain.storage.UserStorageQuotaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserStorageQuotaRepository extends JpaRepository<UserStorageQuota, UserStorageQuotaId> {

    /** 原子增加已用空间和文件数，避免先读后写竞态 */
    @Modifying
    @Query(value = "UPDATE user_storage_quota SET used_bytes = used_bytes + :bytes, " +
                   "file_count = file_count + :count " +
                   "WHERE user_id = :userId AND app_id = :appId",
           nativeQuery = true)
    int incrementUsage(@Param("userId") Long userId,
                       @Param("appId") String appId,
                       @Param("bytes") long bytes,
                       @Param("count") int count);

    /** 原子减少已用空间和文件数（删除时调用，CASE WHEN 保证不降为负数） */
    @Modifying
    @Query(value = "UPDATE user_storage_quota SET " +
                   "used_bytes = CASE WHEN used_bytes > :bytes THEN used_bytes - :bytes ELSE 0 END, " +
                   "file_count = CASE WHEN file_count > :count THEN file_count - :count ELSE 0 END " +
                   "WHERE user_id = :userId AND app_id = :appId",
           nativeQuery = true)
    int decrementUsage(@Param("userId") Long userId,
                       @Param("appId") String appId,
                       @Param("bytes") long bytes,
                       @Param("count") int count);
}
