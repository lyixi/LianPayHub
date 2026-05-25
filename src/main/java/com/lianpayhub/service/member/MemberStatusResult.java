package com.lianpayhub.service.member;

import com.lianpayhub.domain.member.MemberStatus;
import java.time.LocalDateTime;

public class MemberStatusResult {
    private final boolean active;
    private final MemberStatus status;
    private final LocalDateTime expireAt;

    public MemberStatusResult(boolean active, MemberStatus status, LocalDateTime expireAt) {
        this.active = active;
        this.status = status;
        this.expireAt = expireAt;
    }

    public static MemberStatusResult inactive() {
        return new MemberStatusResult(false, null, null);
    }

    public boolean isActive() { return active; }
    public MemberStatus getStatus() { return status; }
    public LocalDateTime getExpireAt() { return expireAt; }
}
