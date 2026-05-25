package com.lianpayhub.service.demo;

import com.lianpayhub.domain.app.AppType;
import com.lianpayhub.domain.device.DeviceInfo;
import com.lianpayhub.domain.packageinfo.PackageInfo;
import com.lianpayhub.domain.packageinfo.PackageType;
import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.repository.AppInfoRepository;
import com.lianpayhub.repository.PackageInfoRepository;
import com.lianpayhub.service.app.AppService;
import com.lianpayhub.service.app.CreateAppCommand;
import com.lianpayhub.service.device.DeviceService;
import com.lianpayhub.service.device.RegisterDeviceCommand;
import com.lianpayhub.service.packageinfo.CreatePackageCommand;
import com.lianpayhub.service.packageinfo.PackageService;
import com.lianpayhub.service.payment.CreateOrderCommand;
import com.lianpayhub.service.payment.CreateOrderResult;
import com.lianpayhub.service.payment.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DemoDataService {

    private static final String DEMO_APP_ID = "demo-device-app";
    private static final String DEMO_DEVICE_CODE = "demo-device-001";

    private final AppInfoRepository appInfoRepository;
    private final PackageInfoRepository packageInfoRepository;
    private final AppService appService;
    private final PackageService packageService;
    private final DeviceService deviceService;
    private final PaymentService paymentService;

    public DemoDataService(AppInfoRepository appInfoRepository,
                           PackageInfoRepository packageInfoRepository,
                           AppService appService,
                           PackageService packageService,
                           DeviceService deviceService,
                           PaymentService paymentService) {
        this.appInfoRepository = appInfoRepository;
        this.packageInfoRepository = packageInfoRepository;
        this.appService = appService;
        this.packageService = packageService;
        this.deviceService = deviceService;
        this.paymentService = paymentService;
    }

    @Transactional
    public DemoDataResult createDeviceVipDemo() {
        if (!appInfoRepository.existsByAppId(DEMO_APP_ID)) {
            appService.createApp(new CreateAppCommand(
                    DEMO_APP_ID,
                    "演示设备码 APP",
                    AppType.DEVICE_ONLY,
                    false,
                    true
            ));
        }

        List<PackageInfo> packages = packageInfoRepository.findByAppId(DEMO_APP_ID);
        PackageInfo packageInfo = packages.isEmpty()
                ? packageService.createPackage(new CreatePackageCommand(
                        DEMO_APP_ID,
                        "演示月度会员",
                        PackageType.MEMBERSHIP,
                        990,
                        30,
                        "演示设备 VIP 权益"
                ))
                : packages.get(0);

        DeviceInfo device = deviceService.registerOrGet(new RegisterDeviceCommand(
                DEMO_APP_ID,
                DEMO_DEVICE_CODE,
                "演示设备",
                "ANDROID",
                "demo-fingerprint"
        ));

        CreateOrderResult order = paymentService.createOrder(new CreateOrderCommand(
                DEMO_APP_ID,
                null,
                device.getId(),
                packageInfo.getId(),
                PayChannel.OTHER
        ));
        paymentService.markPaidForTest(order.getOrderNo(), "DEMO-TRADE-" + System.currentTimeMillis());

        return new DemoDataResult(
                DEMO_APP_ID,
                packageInfo.getId(),
                device.getId(),
                order.getOrderNo(),
                "演示数据已创建，并已手动标记支付成功"
        );
    }
}
