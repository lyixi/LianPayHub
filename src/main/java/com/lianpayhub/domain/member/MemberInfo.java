package com.lianpayhub.domain.member;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_info", indexes = {
        @Index(name = "idx_member_info_user", columnList = "app_id,user_id"),
        @Index(name = "idx_member_info_device", columnList = "app_id,device_id")
})
public class MemberInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_subject_type", nullable = false, length = 32)
    private MemberSubjectType memberSubjectType;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expireAt;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    protected MemberInfo() {
    }

    public MemberInfo(String appId, MemberSubjectType memberSubjectType, Long userId, Long deviceId,
                      Long packageId, LocalDateTime startAt, LocalDateTime expireAt, Long orderId) {
        this.appId = appId;
        this.memberSubjectType = memberSubjectType;
        this.userId = userId;
        this.deviceId = deviceId;
        this.packageId = packageId;
        this.startAt = startAt;
        this.expireAt = expireAt;
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public MemberSubjectType getMemberSubjectType() {
        return memberSubjectType;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void extendTo(LocalDateTime expireAt, Long packageId, Long orderId) {
        this.expireAt = expireAt;
        this.packageId = packageId;
        this.orderId = orderId;
        this.status = MemberStatus.ACTIVE;
    }

    public void cancel() {
        this.status = MemberStatus.CANCELLED;
        this.expireAt = LocalDateTime.now();
    }
}
