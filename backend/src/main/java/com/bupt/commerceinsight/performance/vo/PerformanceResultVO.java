package com.bupt.commerceinsight.performance.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PerformanceResultVO {

    private final String testName;
    private final Integer threadCount;
    private final Integer totalRequests;
    private final Integer successRequests;
    private final Integer failedRequests;
    private final BigDecimal avgLatencyMs;
    private final BigDecimal maxLatencyMs;
    private final BigDecimal minLatencyMs;
    private final BigDecimal throughputQps;
    private final List<PerformanceRecordVO> records;
    private final Map<String, Object> chartData;

    public PerformanceResultVO(String testName, Integer threadCount, Integer totalRequests, Integer successRequests,
                               Integer failedRequests, BigDecimal avgLatencyMs, BigDecimal maxLatencyMs,
                               BigDecimal minLatencyMs, BigDecimal throughputQps,
                               List<PerformanceRecordVO> records, Map<String, Object> chartData) {
        this.testName = testName;
        this.threadCount = threadCount;
        this.totalRequests = totalRequests;
        this.successRequests = successRequests;
        this.failedRequests = failedRequests;
        this.avgLatencyMs = avgLatencyMs;
        this.maxLatencyMs = maxLatencyMs;
        this.minLatencyMs = minLatencyMs;
        this.throughputQps = throughputQps;
        this.records = records;
        this.chartData = chartData;
    }

    public String getTestName() {
        return testName;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public Integer getTotalRequests() {
        return totalRequests;
    }

    public Integer getSuccessRequests() {
        return successRequests;
    }

    public Integer getFailedRequests() {
        return failedRequests;
    }

    public BigDecimal getAvgLatencyMs() {
        return avgLatencyMs;
    }

    public BigDecimal getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public BigDecimal getMinLatencyMs() {
        return minLatencyMs;
    }

    public BigDecimal getThroughputQps() {
        return throughputQps;
    }

    public List<PerformanceRecordVO> getRecords() {
        return records;
    }

    public Map<String, Object> getChartData() {
        return chartData;
    }
}
