package com.lianpayhub.web.adapter;

import com.lianpayhub.common.api.ApiResponse;
import com.lianpayhub.domain.adapter.AdapterReport;
import com.lianpayhub.repository.AdapterReportRepository;
import com.lianpayhub.service.app.AppService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adapter")
public class AdapterController {

    private final AdapterReportRepository adapterReportRepository;
    private final AppService appService;

    public AdapterController(AdapterReportRepository adapterReportRepository, AppService appService) {
        this.adapterReportRepository = adapterReportRepository;
        this.appService = appService;
    }

    @PostMapping("/report")
    public ApiResponse<Void> report(@Valid @RequestBody AdapterReportRequest request) {
        appService.requireEnabledApp(request.appId());
        adapterReportRepository.save(new AdapterReport(
                request.appId(),
                request.sourceId(),
                request.reportType(),
                request.payload()
        ));
        return ApiResponse.ok();
    }

    @GetMapping("/status")
    public ApiResponse<String> status(@RequestParam String appId) {
        appService.requireEnabledApp(appId);
        return ApiResponse.ok("UP");
    }
}
