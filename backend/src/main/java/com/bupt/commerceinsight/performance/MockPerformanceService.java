package com.bupt.commerceinsight.performance;

import com.bupt.commerceinsight.performance.vo.PerformanceRecordVO;
import com.bupt.commerceinsight.performance.vo.PerformanceResultVO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockPerformanceService implements PerformanceService {

    @Override
    public PerformanceResultVO results(String testType) {
        List<PerformanceRecordVO> records = List.of(
            new PerformanceRecordVO(1, new BigDecimal("410.2"), new BigDecimal("2.43")),
            new PerformanceRecordVO(2, new BigDecimal("590.8"), new BigDecimal("3.38")),
            new PerformanceRecordVO(4, new BigDecimal("910.4"), new BigDecimal("4.52")),
            new PerformanceRecordVO(8, new BigDecimal("1260.5"), new BigDecimal("6.35"))
        );
        Map<String, Object> chartData = Map.of(
            "xAxis", records.stream().map(PerformanceRecordVO::getThreadCount).toList(),
            "latencySeries", records.stream().map(PerformanceRecordVO::getAvgLatencyMs).toList(),
            "throughputSeries", records.stream().map(PerformanceRecordVO::getThroughput).toList()
        );
        return new PerformanceResultVO(
            "tpcc".equalsIgnoreCase(testType) ? "TPC-C Concurrent Transaction Test" : "TPC-H Concurrent Query Test",
            8, 80, 80, 0,
            new BigDecimal("1260.5"),
            new BigDecimal("2890.2"),
            new BigDecimal("330.1"),
            new BigDecimal("6.35"),
            records,
            chartData
        );
    }
}
