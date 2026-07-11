package com.bupt.commerceinsight.performance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class PerformanceResultRequest {

    @NotBlank
    @Size(max = 100)
    private String testName;

    @NotBlank
    @Pattern(regexp = "tpch|tpcc")
    private String testType;

    @NotNull
    @Positive
    private Integer threadCount;

    @NotNull
    @PositiveOrZero
    private Integer totalRequests;

    @NotNull
    @PositiveOrZero
    private Integer successCount;

    @NotNull
    @PositiveOrZero
    private Integer failCount;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal avgLatencyMs;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal maxLatencyMs;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal minLatencyMs;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal throughput;

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    public Integer getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Integer totalRequests) {
        this.totalRequests = totalRequests;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public BigDecimal getAvgLatencyMs() {
        return avgLatencyMs;
    }

    public void setAvgLatencyMs(BigDecimal avgLatencyMs) {
        this.avgLatencyMs = avgLatencyMs;
    }

    public BigDecimal getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public void setMaxLatencyMs(BigDecimal maxLatencyMs) {
        this.maxLatencyMs = maxLatencyMs;
    }

    public BigDecimal getMinLatencyMs() {
        return minLatencyMs;
    }

    public void setMinLatencyMs(BigDecimal minLatencyMs) {
        this.minLatencyMs = minLatencyMs;
    }

    public BigDecimal getThroughput() {
        return throughput;
    }

    public void setThroughput(BigDecimal throughput) {
        this.throughput = throughput;
    }
}
