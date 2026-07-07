package com.bupt.commerceinsight.dashboard;

import com.bupt.commerceinsight.common.ApiResponse;
import com.bupt.commerceinsight.dashboard.vo.DashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardVO> summary() {
        return ApiResponse.success(dashboardService.summary());
    }
}
