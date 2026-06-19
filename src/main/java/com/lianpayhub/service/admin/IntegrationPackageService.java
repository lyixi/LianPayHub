package com.lianpayhub.service.admin;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.packageinfo.PackageStatus;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.PackageInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntegrationPackageService {

    private final AppInfoRepository appInfoRepository;
    private final PackageInfoRepository packageInfoRepository;

    public IntegrationPackageService(AppInfoRepository appInfoRepository, PackageInfoRepository packageInfoRepository) {
        this.appInfoRepository = appInfoRepository;
        this.packageInfoRepository = packageInfoRepository;
    }

    @Transactional(readOnly = true)
    public String buildMarkdown(Long id) {
        AppInfo app = appInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "APP 不存在"));
        StringBuilder md = new StringBuilder();
        md.append("# ").append(app.getAppName()).append(" 接入包\n\n");
        md.append("## 基础信息\n\n");
        md.append("| 字段 | 值 |\n|---|---|\n");
        md.append("| appId | `").append(app.getAppId()).append("` |\n");
        md.append("| 类型 | ").append(app.getAppType()).append(" |\n");
        md.append("| 手机号登录 | ").append(app.isNeedMobileLogin() ? "需要" : "不需要").append(" |\n");
        md.append("| 设备码会员 | ").append(app.isNeedDeviceVip() ? "需要" : "不需要").append(" |\n");
        md.append("\n> appSecret 只在创建或重置时展示一次；如已丢失，请在后台重置密钥后更新客户端配置。\n\n");
        md.append("## 常用接口\n\n");
        md.append("| 场景 | 方法 | 路径 |\n|---|---|---|\n");
        md.append("| 套餐列表 | GET | `/api/packages?appId=").append(app.getAppId()).append("` |\n");
        md.append("| 设备注册 | POST | `/api/device/register` |\n");
        md.append("| 启动上报 | POST | `/api/device/launch` |\n");
        md.append("| 会员状态 | GET | `/api/member/status?appId=").append(app.getAppId()).append("&deviceCode=<deviceCode>` |\n");
        md.append("| 创建订单 | POST | `/api/payment/create-order` |\n");
        if (app.isNeedMobileLogin()) {
            md.append("| 发送验证码 | POST | `/api/auth/send-code` |\n");
            md.append("| 手机号登录 | POST | `/api/auth/login` |\n");
        }
        md.append("\n## curl 示例\n\n");
        md.append("```bash\n");
        md.append("curl -X GET 'http://localhost:8888/api/packages?appId=").append(app.getAppId()).append("'\n\n");
        md.append("curl -X POST 'http://localhost:8888/api/device/register' \\\n");
        md.append("  -H 'Content-Type: application/json' \\\n");
        md.append("  -d '{\"appId\":\"").append(app.getAppId()).append("\",\"deviceCode\":\"device-001\",\"deviceName\":\"测试设备\"}'\n\n");
        md.append("curl -X POST 'http://localhost:8888/api/device/launch' \\\n");
        md.append("  -H 'Content-Type: application/json' \\\n");
        md.append("  -d '{\"appId\":\"").append(app.getAppId()).append("\",\"deviceCode\":\"device-001\",\"sessionId\":\"sess-current\",\"previousSessionId\":\"sess-last\",\"previousSessionEndAt\":\"2026-06-14T10:25:30\",\"previousDurationSeconds\":1530}'\n\n");
        md.append("curl -X POST 'http://localhost:8888/api/payment/create-order' \\\n");
        md.append("  -H 'Content-Type: application/json' \\\n");
        md.append("  -d '{\"appId\":\"").append(app.getAppId()).append("\",\"deviceCode\":\"device-001\",\"packageId\":1,\"payChannel\":\"ALIPAY\"}'\n");
        md.append("```\n\n");
        md.append("## 启动会话规则\n\n");
        md.append("| 字段 | 说明 |\n|---|---|\n");
        md.append("| sessionId | 本次启动生成的唯一会话 ID |\n");
        md.append("| previousSessionId | 上次启动保存的会话 ID，下次启动补退出时传入 |\n");
        md.append("| previousSessionEndAt | 上次退出时间；后台匹配到 previousSessionId 对应 LAUNCH 后回填退出时间和时长 |\n\n");
        md.append("## 当前启用套餐\n\n");
        md.append("| ID | 名称 | 价格/分 | 天数 | 权益 |\n|---:|---|---:|---:|---|\n");
        packageInfoRepository.findByAppIdAndStatus(app.getAppId(), PackageStatus.ENABLED).forEach(p ->
                md.append("| ").append(p.getId()).append(" | ")
                        .append(p.getPackageName()).append(" | ")
                        .append(p.getPriceCents()).append(" | ")
                        .append(p.getDurationDays()).append(" | ")
                        .append(p.getBenefitsText() == null ? "" : p.getBenefitsText().replace("|", "\\|"))
                        .append(" |\n")
        );
        return md.toString();
    }
}
