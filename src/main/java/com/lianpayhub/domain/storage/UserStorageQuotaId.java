package com.lianpayhub.domain.storage;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserStorageQuotaId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    protected UserStorageQuotaId() {
    }

    public UserStorageQuotaId(Long userId, String appId) {
        this.userId = userId;
        this.appId = appId;
    }

    public Long getUserId() { return userId; }
    public String getAppId() { return appId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserStorageQuotaId)) return false;
        UserStorageQuotaId that = (UserStorageQuotaId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(appId, that.appId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, appId);
    }
}
