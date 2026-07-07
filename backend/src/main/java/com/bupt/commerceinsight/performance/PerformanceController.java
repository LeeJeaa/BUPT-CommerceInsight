package com.bupt.commerceinsight.performance;

import com.bupt.commerceinsight.common.ApiResponse;
import com.bupt.commerceinsight.performance.vo.PerformanceResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping("/results")
    public ApiResponse<PerformanceResultVO> results(@RequestParam(required = false) String testType) {
        return ApiResponse.success(performanceService.results(testType));
    }
}
