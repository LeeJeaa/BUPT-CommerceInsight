package com.bupt.commerceinsight.performance;

import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import com.bupt.commerceinsight.performance.vo.PerformanceRecordVO;
import com.bupt.commerceinsight.performance.vo.PerformanceResultVO;
import com.bupt.commerceinsight.performance.dto.PerformanceResultRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev", "prod"})
public class JdbcPerformanceService implements PerformanceService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPerformanceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PerformanceResultVO results(String testType) {
        String normalizedType = testType == null || testType.isBlank() ? "tpch" : testType;
        validateTestType(normalizedType);
        List<PerformanceRow> rows = jdbcTemplate.query("""
            SELECT test_name, thread_count, total_requests, success_count, fail_count,
                   avg_latency_ms, max_latency_ms, min_latency_ms, throughput
            FROM performance_result
            WHERE test_type = ?
            ORDER BY created_at, result_id
            """, (resultSet, rowNum) -> new PerformanceRow(
                resultSet.getString("test_name"),
                resultSet.getInt("thread_count"),
                resultSet.getInt("total_requests"),
                resultSet.getInt("success_count"),
                resultSet.getInt("fail_count"),
                resultSet.getBigDecimal("avg_latency_ms"),
                resultSet.getBigDecimal("max_latency_ms"),
                resultSet.getBigDecimal("min_latency_ms"),
                resultSet.getBigDecimal("throughput")
            ), normalizedType);
        if (rows.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "暂无性能测试结果");
        }
        PerformanceRow summary = rows.get(rows.size() - 1);
        List<PerformanceRecordVO> records = rows.stream()
            .map(row -> new PerformanceRecordVO(row.threadCount(), row.avgLatencyMs(), row.throughput()))
            .toList();
        Map<String, Object> chartData = Map.of(
            "xAxis", records.stream().map(PerformanceRecordVO::getThreadCount).toList(),
            "latencySeries", records.stream().map(PerformanceRecordVO::getAvgLatencyMs).toList(),
            "throughputSeries", records.stream().map(PerformanceRecordVO::getThroughput).toList()
        );
        return new PerformanceResultVO(
            summary.testName(), summary.threadCount(), summary.totalRequests(),
            summary.successCount(), summary.failCount(), summary.avgLatencyMs(),
            summary.maxLatencyMs(), summary.minLatencyMs(), summary.throughput(),
            records, chartData
        );
    }

    @Override
    public PerformanceResultVO save(PerformanceResultRequest request) {
        validateRequest(request);
        jdbcTemplate.update("""
            INSERT INTO performance_result (
                test_name, test_type, thread_count, total_requests, success_count, fail_count,
                avg_latency_ms, max_latency_ms, min_latency_ms, throughput
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, request.getTestName(), request.getTestType(), request.getThreadCount(),
            request.getTotalRequests(), request.getSuccessCount(), request.getFailCount(),
            request.getAvgLatencyMs(), request.getMaxLatencyMs(), request.getMinLatencyMs(),
            request.getThroughput());
        return results(request.getTestType());
    }

    private void validateRequest(PerformanceResultRequest request) {
        validateTestType(request.getTestType());
        if ((long) request.getSuccessCount() + request.getFailCount() != request.getTotalRequests()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "successCount + failCount 必须等于 totalRequests");
        }
        if (request.getMinLatencyMs().compareTo(request.getAvgLatencyMs()) > 0
            || request.getAvgLatencyMs().compareTo(request.getMaxLatencyMs()) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "延迟必须满足 minLatencyMs <= avgLatencyMs <= maxLatencyMs");
        }
    }

    private void validateTestType(String testType) {
        if (!Set.of("tpch", "tpcc").contains(testType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "testType 仅支持 tpch 或 tpcc");
        }
    }

    private record PerformanceRow(
        String testName,
        Integer threadCount,
        Integer totalRequests,
        Integer successCount,
        Integer failCount,
        java.math.BigDecimal avgLatencyMs,
        java.math.BigDecimal maxLatencyMs,
        java.math.BigDecimal minLatencyMs,
        java.math.BigDecimal throughput
    ) {
    }
}
