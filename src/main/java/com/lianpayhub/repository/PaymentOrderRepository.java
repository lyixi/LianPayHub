package com.lianpayhub.repository;

import com.lianpayhub.domain.payment.PaymentOrder;
import com.lianpayhub.domain.payment.PayStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByOrderNo(String orderNo);
    Page<PaymentOrder> findByAppId(String appId, Pageable pageable);
    Page<PaymentOrder> findByUserId(Long userId, Pageable pageable);
    List<PaymentOrder> findTop10ByUserIdOrderByIdDesc(Long userId);

    @Query("select o from PaymentOrder o " +
            "left join DeviceInfo d on d.id = o.deviceId " +
            "left join UserInfo u on u.id = o.userId " +
            "where (?1 is null or ?1 = '' or o.appId = ?1) " +
            "and (?2 is null or ?2 = '' " +
            "or lower(o.orderNo) like lower(concat('%', ?2, '%')) " +
            "or lower(coalesce(o.tradeNo, '')) like lower(concat('%', ?2, '%')) " +
            "or lower(coalesce(o.channelOrderNo, '')) like lower(concat('%', ?2, '%')) " +
            "or lower(coalesce(d.deviceCode, '')) like lower(concat('%', ?2, '%')) " +
            "or lower(coalesce(u.mobile, '')) like lower(concat('%', ?2, '%'))) ")
    Page<PaymentOrder> search(String appId, String keyword, Pageable pageable);

    List<PaymentOrder> findTop10ByAppIdOrderByIdDesc(String appId);
    List<PaymentOrder> findTop10ByDeviceIdOrderByIdDesc(Long deviceId);
    List<PaymentOrder> findTop100ByPayStatusAndExpireAtBeforeOrderByExpireAtAsc(PayStatus payStatus, LocalDateTime expireAt);
    long countByAppId(String appId);
    long countByUserId(Long userId);
    void deleteByAppId(String appId);
    long countByDeviceId(Long deviceId);
    long countByUserIdAndPayStatus(Long userId, PayStatus payStatus);
    long countByAppIdAndPayStatus(String appId, PayStatus payStatus);
    long countByDeviceIdAndPayStatus(Long deviceId, PayStatus payStatus);
    long countByPayStatus(PayStatus payStatus);

    @Query("select coalesce(sum(o.amountCents), 0) from PaymentOrder o where o.payStatus = ?1")
    Long sumAmountCentsByPayStatus(PayStatus payStatus);

    @Query("select coalesce(sum(o.amountCents), 0) from PaymentOrder o where o.appId = ?1 and o.payStatus = ?2")
    Long sumAmountCentsByAppIdAndPayStatus(String appId, PayStatus payStatus);

    @Query("select coalesce(sum(o.amountCents), 0) from PaymentOrder o where o.userId = ?1 and o.payStatus = ?2")
    Long sumAmountCentsByUserIdAndPayStatus(Long userId, PayStatus payStatus);

    @Query("select coalesce(sum(o.amountCents), 0) from PaymentOrder o where o.deviceId = ?1 and o.payStatus = ?2")
    Long sumAmountCentsByDeviceIdAndPayStatus(Long deviceId, PayStatus payStatus);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    long countByAppIdAndCreatedAtBetween(String appId, LocalDateTime start, LocalDateTime end);
    long countByPayStatusAndPaidAtBetween(PayStatus payStatus, LocalDateTime start, LocalDateTime end);
    long countByUserIdAndPayStatusAndPaidAtBetween(Long userId, PayStatus payStatus, LocalDateTime start, LocalDateTime end);
    long countByAppIdAndPayStatusAndPaidAtBetween(String appId, PayStatus payStatus, LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(o.amountCents), 0) from PaymentOrder o where o.payStatus = ?1 and o.paidAt >= ?2 and o.paidAt < ?3")
    Long sumAmountCentsByPayStatusAndPaidAtBetween(PayStatus payStatus, LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(o.amountCents), 0) from PaymentOrder o where o.appId = ?1 and o.payStatus = ?2 and o.paidAt >= ?3 and o.paidAt < ?4")
    Long sumAmountCentsByAppIdAndPayStatusAndPaidAtBetween(String appId, PayStatus payStatus, LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(o.amountCents), 0) from PaymentOrder o where o.userId = ?1 and o.payStatus = ?2 and o.paidAt >= ?3 and o.paidAt < ?4")
    Long sumAmountCentsByUserIdAndPayStatusAndPaidAtBetween(Long userId, PayStatus payStatus, LocalDateTime start, LocalDateTime end);

    @Query("select o.appId, count(o), " +
            "sum(case when o.payStatus = ?1 then 1 else 0 end), " +
            "coalesce(sum(case when o.payStatus = ?1 then o.amountCents else 0 end), 0) " +
            "from PaymentOrder o group by o.appId")
    List<Object[]> summarizeByApp(PayStatus paidStatus);

    @Query("select o.payChannel, count(o), " +
            "sum(case when o.payStatus = ?1 then 1 else 0 end), " +
            "coalesce(sum(case when o.payStatus = ?1 then o.amountCents else 0 end), 0) " +
            "from PaymentOrder o group by o.payChannel")
    List<Object[]> summarizeByPayChannel(PayStatus paidStatus);
}
