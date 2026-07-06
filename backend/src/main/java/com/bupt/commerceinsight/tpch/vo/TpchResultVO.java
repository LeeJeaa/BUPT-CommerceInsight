package com.bupt.commerceinsight.tpch.vo;

import java.util.List;
import java.util.Map;

public class TpchResultVO<T> {

    private final String queryName;
    private final Long elapsedMs;
    private final Integer rowCount;
    private final List<T> records;
    private final Map<String, Object> chartData;
    private final String explainPlan;

    public TpchResultVO(String queryName, Long elapsedMs, Integer rowCount, List<T> records,
                        Map<String, Object> chartData, String explainPlan) {
        this.queryName = queryName;
        this.elapsedMs = elapsedMs;
        this.rowCount = rowCount;
        this.records = records;
        this.chartData = chartData;
        this.explainPlan = explainPlan;
    }

    public String getQueryName() {
        return queryName;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public List<T> getRecords() {
        return records;
    }

    public Map<String, Object> getChartData() {
        return chartData;
    }

    public String getExplainPlan() {
        return explainPlan;
    }
}
