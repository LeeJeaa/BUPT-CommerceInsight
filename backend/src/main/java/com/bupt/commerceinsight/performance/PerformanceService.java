package com.bupt.commerceinsight.performance;

import com.bupt.commerceinsight.performance.vo.PerformanceResultVO;

public interface PerformanceService {

    PerformanceResultVO results(String testType);
}
