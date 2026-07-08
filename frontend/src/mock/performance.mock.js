const dRawPerformance = {
  summary: {
    testName: 'mock_tpch_q1_sf01',
    threadCount: 4,
    totalRequests: 50,
    successRequests: 50,
    failedRequests: 0,
    avgLatencyMs: 128.4,
    maxLatencyMs: 211.9,
    minLatencyMs: 91.7,
    throughput: 29.6
  },
  chartData: {
    xAxis: [1, 2, 4, 8],
    avgLatencyMs: [94.1, 107.8, 128.4, 176.5],
    throughput: [10.2, 19.4, 29.6, 35.1]
  }
}

export function normalizePerformance(raw = dRawPerformance) {
  if (raw.data?.successCount !== undefined) {
    return raw
  }
  const summary = raw.summary || raw
  const xAxis = raw.chartData?.xAxis || [summary.threadCount]
  const latencySeries = raw.chartData?.latencySeries || raw.chartData?.avgLatencyMs || [summary.avgLatencyMs]
  const throughputSeries = raw.chartData?.throughputSeries || raw.chartData?.throughput || [summary.throughput]
  return {
    code: 200,
    message: 'success',
    data: {
      testName: summary.testName,
      threadCount: summary.threadCount,
      totalRequests: summary.totalRequests,
      successCount: summary.successCount ?? summary.successRequests,
      failCount: summary.failCount ?? summary.failedRequests,
      avgLatencyMs: summary.avgLatencyMs,
      maxLatencyMs: summary.maxLatencyMs,
      minLatencyMs: summary.minLatencyMs,
      throughput: summary.throughput,
      records: xAxis.map((threadCount, index) => ({
        threadCount,
        avgLatencyMs: latencySeries[index],
        throughput: throughputSeries[index]
      })),
      chartData: {
        xAxis,
        latencySeries,
        throughputSeries
      }
    }
  }
}

export const performanceMock = normalizePerformance()
