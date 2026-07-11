package com.bupt.commerceinsight.performance;

import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import com.bupt.commerceinsight.performance.dto.PerformanceResultRequest;
import com.bupt.commerceinsight.performance.vo.PerformanceRecordVO;
import com.bupt.commerceinsight.performance.vo.PerformanceResultVO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockPerformanceService implements PerformanceService {

    private final Map<String, PerformanceResultVO> savedResults = new ConcurrentHashMap<>();

    @Override
    public PerformanceResultVO results(String testType) {
        String normalizedType = testType == null || testType.isBlank() ? "tpch" : testType;
        validateTestType(normalizedType);
        PerformanceResultVO saved = savedResults.get(normalizedType);
        if (saved != null) {
            return saved;
        }
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
            "tpcc".equals(normalizedType) ? "TPC-C Concurrent Transaction Test" : "TPC-H Concurrent Query Test",
            8, 80, 80, 0,
            new BigDecimal("1260.5"),
            new BigDecimal("2890.2"),
            new BigDecimal("330.1"),
            new BigDecimal("6.35"),
            records,
            chartData
        );
    }

    @Override
    public PerformanceResultVO save(PerformanceResultRequest request) {
        validateTestType(request.getTestType());
        if ((long) request.getSuccessCount() + request.getFailCount() != request.getTotalRequests()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "successCount + failCount 必须等于 totalRequests");
        }
        if (request.getMinLatencyMs().compareTo(request.getAvgLatencyMs()) > 0
            || request.getAvgLatencyMs().compareTo(request.getMaxLatencyMs()) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "延迟必须满足 minLatencyMs <= avgLatencyMs <= maxLatencyMs");
        }
        List<PerformanceRecordVO> records = List.of(new PerformanceRecordVO(
            request.getThreadCount(), request.getAvgLatencyMs(), request.getThroughput()
        ));
        PerformanceResultVO result = new PerformanceResultVO(
            request.getTestName(), request.getThreadCount(), request.getTotalRequests(),
            request.getSuccessCount(), request.getFailCount(), request.getAvgLatencyMs(),
            request.getMaxLatencyMs(), request.getMinLatencyMs(), request.getThroughput(),
            records,
            Map.of(
                "xAxis", List.of(request.getThreadCount()),
                "latencySeries", List.of(request.getAvgLatencyMs()),
                "throughputSeries", List.of(request.getThroughput())
            )
        );
        savedResults.put(request.getTestType(), result);
        return result;
    }

    private void validateTestType(String testType) {
        if (!Set.of("tpch", "tpcc").contains(testType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "testType 仅支持 tpch 或 tpcc");
        }
    }
}
