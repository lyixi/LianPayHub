package com.lianpayhub.domain.user;

import com.lianpayhub.domain.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "user_info", indexes = {
        @Index(name = "idx_user_info_mobile", columnList = "mobile", unique = true)
})
public class UserInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32, unique = true)
    private String mobile;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 32)
    private UserType userType = UserType.ACCOUNT;

    @Column(name = "open_id", length = 128)
    private String openId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserStatus status = UserStatus.ENABLED;

    protected UserInfo() {
    }

    public UserInfo(String mobile) {
        this.mobile = mobile;
    }

    public Long getId() {
        return id;
    }

    public String getMobile() {
        return mobile;
    }

    public UserType getUserType() {
        return userType;
    }

    public String getOpenId() {
        return openId;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isEnabled() {
        return status == UserStatus.ENABLED;
    }

    public void changeStatus(UserStatus status) {
        this.status = status;
    }
}
