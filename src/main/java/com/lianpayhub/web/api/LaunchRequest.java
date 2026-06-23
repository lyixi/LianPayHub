package com.lianpayhub.web.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;

@Schema(description = "设备启动上报请求；可在下次启动时补传上次退出数据，用于统计上次使用时长")
public class LaunchRequest {
    @Schema(description = "APP 标识", example = "demo-app", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String appId;
    @Schema(description = "设备稳定唯一编码", example = "device-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String deviceCode;
    @Schema(description = "关联用户 ID，可选", example = "1")
    private Long userId;
    @Schema(description = "客户端平台", example = "android")
    private String platform;
    @Schema(description = "客户端分支，可选", example = "stable")
    private String branch;
    @Schema(description = "客户端渠道，可选", example = "official")
    private String channel;
    @Schema(description = "客户端环境，可选", example = "prod")
    private String platformEnvironment;
    @Schema(description = "版本名称，可选", example = "1.0.0")
    private String versionName;
    @Schema(description = "版本号，可选", example = "100")
    private String versionCode;
    @Schema(description = "客户端版本", example = "1.0.0")
    private String version;
    @Schema(description = "网络类型", example = "wifi")
    private String networkType;
    @Schema(description = "客户端 IP，可由接入方传入", example = "127.0.0.1")
    private String ipAddress;
    @Schema(description = "本次启动会话 ID；客户端每次启动生成并持久化到下次启动补传", example = "sess-20260614-001")
    private String sessionId;
    @Schema(description = "上次启动会话 ID；下次启动时补传，后台据此绑定 EXIT 与对应 LAUNCH", example = "sess-20260614-000")
    private String previousSessionId;
    @Schema(description = "上次启动时间；下次启动时补传，异常退出拿不到可不传", example = "2026-06-14T10:00:00")
    private LocalDateTime previousSessionStartAt;
    @Schema(description = "上次退出时间；有值时后台会补一条 EXIT 记录", example = "2026-06-14T10:25:30")
    private LocalDateTime previousSessionEndAt;
    @Schema(description = "上次使用时长秒数；不传时后台尝试用开始/结束时间计算", example = "1530")
    private Long previousDurationSeconds;
    @Schema(description = "扩展 JSON 字符串", example = "{\"channel\":\"appstore\"}")
    private String eventData;

    public String appId() { return appId; }
    public String deviceCode() { return deviceCode; }
    public Long userId() { return userId; }
    public String platform() { return platform; }
    public String branch() { return branch; }
    public String channel() { return channel; }
    public String platformEnvironment() { return platformEnvironment; }
    public String versionName() { return versionName; }
    public String versionCode() { return versionCode; }
    public String version() { return version; }
    public String networkType() { return networkType; }
    public String ipAddress() { return ipAddress; }
    public String sessionId() { return sessionId; }
    public String previousSessionId() { return previousSessionId; }
    public LocalDateTime previousSessionStartAt() { return previousSessionStartAt; }
    public LocalDateTime previousSessionEndAt() { return previousSessionEndAt; }
    public Long previousDurationSeconds() { return previousDurationSeconds; }
    public String eventData() { return eventData; }
}
