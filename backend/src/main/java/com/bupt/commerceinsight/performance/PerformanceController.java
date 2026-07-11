package com.bupt.commerceinsight.performance;

import com.bupt.commerceinsight.common.ApiResponse;
import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import com.bupt.commerceinsight.config.AuthContext;
import com.bupt.commerceinsight.performance.dto.PerformanceResultRequest;
import com.bupt.commerceinsight.performance.vo.PerformanceResultVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/results")
    public ApiResponse<PerformanceResultVO> save(@Valid @RequestBody PerformanceResultRequest request) {
        if (!AuthContext.requireUser().isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限");
        }
        return ApiResponse.success(performanceService.save(request));
    }
}
