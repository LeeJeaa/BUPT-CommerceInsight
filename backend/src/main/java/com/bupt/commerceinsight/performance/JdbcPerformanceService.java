package com.bupt.commerceinsight.performance;

import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import com.bupt.commerceinsight.performance.vo.PerformanceRecordVO;
import com.bupt.commerceinsight.performance.vo.PerformanceResultVO;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class JdbcPerformanceService implements PerformanceService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPerformanceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PerformanceResultVO results(String testType) {
        String normalizedType = testType == null || testType.isBlank() ? "tpch" : testType;
        List<PerformanceRow> rows = jdbcTemplate.query("""
            SELECT test_name, thread_count, total_requests, success_count, fail_count,
                   avg_latency_ms, max_latency_ms, min_latency_ms, throughput
            FROM performance_result
            WHERE test_type = ?
            ORDER BY created_at, thread_count
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
