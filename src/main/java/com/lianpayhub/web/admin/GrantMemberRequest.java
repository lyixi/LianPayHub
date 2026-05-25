package com.lianpayhub.web.admin;

import com.lianpayhub.domain.member.MemberSubjectType;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class GrantMemberRequest {
    @NotBlank
    private String appId;
    @NotNull
    private MemberSubjectType subjectType;
    private Long userId;
    private Long deviceId;
    @NotNull
    private Long packageId;
    @NotNull
    @Min(1)
    private Integer durationDays;

    public String appId() { return appId; }
    public MemberSubjectType subjectType() { return subjectType; }
    public Long userId() { return userId; }
    public Long deviceId() { return deviceId; }
    public Long packageId() { return packageId; }
    public Integer durationDays() { return durationDays; }
}
