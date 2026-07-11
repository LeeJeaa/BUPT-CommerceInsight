const performanceData = {
  tpch: {
    testName: 'TPC-H Concurrent Query Test',
    testType: 'tpch',
    threadCount: 8,
    totalRequests: 80,
    successCount: 80,
    failCount: 0,
    avgLatencyMs: 1260.5,
    maxLatencyMs: 2890.2,
    minLatencyMs: 330.1,
    throughput: 6.35,
    records: [
      { threadCount: 1, avgLatencyMs: 410.2, throughput: 2.43 },
      { threadCount: 2, avgLatencyMs: 590.8, throughput: 3.38 },
      { threadCount: 4, avgLatencyMs: 910.4, throughput: 4.52 },
      { threadCount: 8, avgLatencyMs: 1260.5, throughput: 6.35 }
    ],
    chartData: {
      xAxis: [1, 2, 4, 8],
      latencySeries: [410.2, 590.8, 910.4, 1260.5],
      throughputSeries: [2.43, 3.38, 4.52, 6.35]
    }
  },
  tpcc: {
    testName: 'TPC-C Transaction Test',
    testType: 'tpcc',
    threadCount: 8,
    totalRequests: 80,
    successCount: 78,
    failCount: 2,
    avgLatencyMs: 148.6,
    maxLatencyMs: 311.4,
    minLatencyMs: 72.5,
    throughput: 42.8,
    records: [
      { threadCount: 1, avgLatencyMs: 92.4, throughput: 10.8 },
      { threadCount: 2, avgLatencyMs: 116.7, throughput: 20.4 },
      { threadCount: 4, avgLatencyMs: 135.2, throughput: 32.6 },
      { threadCount: 8, avgLatencyMs: 148.6, throughput: 42.8 }
    ],
    chartData: {
      xAxis: [1, 2, 4, 8],
      latencySeries: [92.4, 116.7, 135.2, 148.6],
      throughputSeries: [10.8, 20.4, 32.6, 42.8]
    }
  }
}

export function buildPerformanceMock(testType = 'tpch') {
  return {
    code: 200,
    message: 'success',
    data: performanceData[testType] || performanceData.tpch
  }
}

export function normalizePerformance(raw = buildPerformanceMock()) {
  if (raw?.data) {
    return {
      ...raw,
      data: normalizePerformanceData(raw.data)
    }
  }
  return {
    code: 200,
    message: 'success',
    data: normalizePerformanceData(raw)
  }
}

function normalizePerformanceData(raw = {}) {
  const summary = raw.summary || raw
  const chartData = raw.chartData || {}
  const xAxis = chartData.xAxis || raw.records?.map((item) => item.threadCount) || [summary.threadCount].filter(Boolean)
  const latencySeries = chartData.latencySeries || chartData.avgLatencyMs || raw.records?.map((item) => item.avgLatencyMs) || []
  const throughputSeries = chartData.throughputSeries || chartData.throughput || raw.records?.map((item) => item.throughput) || []
  const records = raw.records || xAxis.map((threadCount, index) => ({
    threadCount,
    avgLatencyMs: latencySeries[index],
    throughput: throughputSeries[index]
  }))

  return {
    testName: summary.testName || '',
    testType: summary.testType || 'tpch',
    threadCount: summary.threadCount,
    totalRequests: summary.totalRequests,
    successCount: summary.successCount ?? summary.successRequests ?? 0,
    failCount: summary.failCount ?? summary.failedRequests ?? 0,
    avgLatencyMs: summary.avgLatencyMs,
    maxLatencyMs: summary.maxLatencyMs,
    minLatencyMs: summary.minLatencyMs,
    throughput: summary.throughput,
    records,
    chartData: {
      xAxis,
      latencySeries,
      throughputSeries
    }
  }
}
