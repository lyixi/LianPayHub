package com.lianpayhub.domain.user;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_app_binding", indexes = {
        @Index(name = "idx_user_app_binding_user_app", columnList = "user_id,app_id", unique = true)
})
public class UserAppBinding extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Enumerated(EnumType.STRING)
    @Column(name = "bind_type", nullable = false, length = 32)
    private BindType bindType;

    @Column(name = "bind_at", nullable = false)
    private LocalDateTime bindAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BindingStatus status = BindingStatus.ENABLED;

    protected UserAppBinding() {
    }

    public UserAppBinding(Long userId, String appId, BindType bindType) {
        this.userId = userId;
        this.appId = appId;
        this.bindType = bindType;
        this.bindAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAppId() {
        return appId;
    }

    public BindType getBindType() {
        return bindType;
    }

    public LocalDateTime getBindAt() {
        return bindAt;
    }

    public BindingStatus getStatus() {
        return status;
    }

    public void changeStatus(BindingStatus status) {
        this.status = status;
    }
}
