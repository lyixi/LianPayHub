package com.lianpayhub.service.member;

import com.lianpayhub.domain.member.MemberSubjectType;

public class GrantMemberCommand {
    private final String appId;
    private final MemberSubjectType subjectType;
    private final Long userId;
    private final Long deviceId;
    private final Long packageId;
    private final Integer durationDays;

    public GrantMemberCommand(String appId, MemberSubjectType subjectType, Long userId, Long deviceId,
                              Long packageId, Integer durationDays) {
        this.appId = appId;
        this.subjectType = subjectType;
        this.userId = userId;
        this.deviceId = deviceId;
        this.packageId = packageId;
        this.durationDays = durationDays;
    }

    public String appId() { return appId; }
    public MemberSubjectType subjectType() { return subjectType; }
    public Long userId() { return userId; }
    public Long deviceId() { return deviceId; }
    public Long packageId() { return packageId; }
    public Integer durationDays() { return durationDays; }
}
