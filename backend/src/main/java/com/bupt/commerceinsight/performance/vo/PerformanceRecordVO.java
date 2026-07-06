package com.bupt.commerceinsight.performance.vo;

import java.math.BigDecimal;

public class PerformanceRecordVO {

    private final Integer threadCount;
    private final BigDecimal avgLatencyMs;
    private final BigDecimal throughputQps;

    public PerformanceRecordVO(Integer threadCount, BigDecimal avgLatencyMs, BigDecimal throughputQps) {
        this.threadCount = threadCount;
        this.avgLatencyMs = avgLatencyMs;
        this.throughputQps = throughputQps;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public BigDecimal getAvgLatencyMs() {
        return avgLatencyMs;
    }

    public BigDecimal getThroughputQps() {
        return throughputQps;
    }
}
