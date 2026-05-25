package com.lianpayhub.web.admin;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.service.demo.DemoDataResult;
import com.lianpayhub.service.demo.DemoDataService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/demo")
public class AdminDemoController {

    private final DemoDataService demoDataService;

    public AdminDemoController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @PostMapping("/device-vip")
    public ApiResponse<DemoDataResult> createDeviceVipDemo() {
        return ApiResponse.ok(demoDataService.createDeviceVipDemo());
    }
}
