package com.bupt.commerceinsight.performance.vo;

import java.math.BigDecimal;

public class PerformanceRecordVO {

    private final Integer threadCount;
    private final BigDecimal avgLatencyMs;
    private final BigDecimal throughput;

    public PerformanceRecordVO(Integer threadCount, BigDecimal avgLatencyMs, BigDecimal throughput) {
        this.threadCount = threadCount;
        this.avgLatencyMs = avgLatencyMs;
        this.throughput = throughput;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public BigDecimal getAvgLatencyMs() {
        return avgLatencyMs;
    }

    public BigDecimal getThroughput() {
        return throughput;
    }
}
