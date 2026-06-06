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

    /** 按精确虚拟路径查找（含已删除，用于覆盖写入判断） */
    Optional<UserFile> findByUserIdAndAppIdAndVirtualPath(Long userId, String appId, String virtualPath);

    /** 增量同步：查询 updated_at 大于指定时间的所有变更（含删除记录） */
    @Query("SELECT f FROM UserFile f WHERE f.userId = :userId AND f.appId = :appId " +
           "AND f.updatedAt > :since ORDER BY f.updatedAt ASC")
    List<UserFile> findChangedSince(@Param("userId") Long userId,
                                    @Param("appId") String appId,
                                    @Param("since") LocalDateTime since);

    /** 统计活跃文件数量（不含软删除） */
    int countByUserIdAndAppIdAndDeletedAtIsNull(Long userId, String appId);
}
