package com.bupt.commerceinsight.performance;

import com.bupt.commerceinsight.performance.vo.PerformanceResultVO;
import com.bupt.commerceinsight.performance.dto.PerformanceResultRequest;

public interface PerformanceService {

    PerformanceResultVO results(String testType);

    PerformanceResultVO save(PerformanceResultRequest request);
}
