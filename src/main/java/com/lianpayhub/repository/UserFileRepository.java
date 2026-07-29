package com.lianpayhub.repository;

import com.lianpayhub.domain.storage.UserFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserFileRepository extends JpaRepository<UserFile, Long> {

    /** 查询某目录下的直接子项（不含软删除） */
    @Query("SELECT f FROM UserFile f WHERE f.userId = :userId AND f.appId = :appId " +
           "AND f.virtualPath LIKE :pathPrefix AND f.deletedAt IS NULL")
    List<UserFile> findByDirectory(@Param("userId") Long userId,
                                   @Param("appId") String appId,
                                   @Param("pathPrefix") String pathPrefix);

    Page<UserFile> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);
    Page<UserFile> findByUserIdAndAppIdAndDeletedAtIsNull(Long userId, String appId, Pageable pageable);

    /** 按精确虚拟路径查找（含已删除，用于覆盖写入判断） */
    Optional<UserFile> findByUserIdAndAppIdAndVirtualPathHashAndVirtualPath(Long userId,
                                                                            String appId,
                                                                            String virtualPathHash,
                                                                            String virtualPath);

    /** 增量同步：查询 updated_at 大于指定时间的所有变更（含删除记录） */
    @Query("SELECT f FROM UserFile f WHERE f.userId = :userId AND f.appId = :appId " +
           "AND f.updatedAt > :since ORDER BY f.updatedAt ASC")
    List<UserFile> findChangedSince(@Param("userId") Long userId,
                                    @Param("appId") String appId,
                                    @Param("since") LocalDateTime since);

    /** 统计活跃文件数量（不含软删除） */
    int countByUserIdAndAppIdAndDeletedAtIsNull(Long userId, String appId);
    int countByUserIdAndDeletedAtIsNull(Long userId);
    long countByUserIdAndAppId(Long userId, String appId);

    @Query("select coalesce(sum(f.sizeBytes), 0) from UserFile f where f.userId = :userId and f.deletedAt is null")
    Long sumSizeBytesByUserId(@Param("userId") Long userId);

    @Query("select coalesce(sum(f.sizeBytes), 0) from UserFile f where f.userId = :userId and f.appId = :appId and f.deletedAt is null")
    Long sumSizeBytesByUserIdAndAppId(@Param("userId") Long userId, @Param("appId") String appId);
}
