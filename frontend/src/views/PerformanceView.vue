<template>
  <div class="page">
    <h2 class="page-title">性能分析</h2>
    <div class="toolbar">
      <el-select v-model="testType" style="width: 220px">
        <el-option label="TPC-H 并发查询" value="tpch" />
        <el-option label="TPC-C 并发事务" value="tpcc" />
      </el-select>
      <el-button type="primary" @click="load">刷新结果</el-button>
    </div>
    <div v-if="result" class="metric-grid">
      <MetricCard label="线程数" :value="result.threadCount" icon="odometer" />
      <MetricCard label="总请求" :value="result.totalRequests" icon="dashboard" />
      <MetricCard label="成功/失败" :value="`${result.successCount}/${result.failCount}`" icon="tickets" />
      <MetricCard label="吞吐量" :value="result.throughput" hint="QPS/TPS" icon="trend" />
      <MetricCard label="平均延迟(ms)" :value="result.avgLatencyMs" icon="timer" />
      <MetricCard label="最大延迟(ms)" :value="result.maxLatencyMs" icon="timer" />
    </div>
    <div v-if="result" class="chart-grid">
      <ChartPanel title="线程数-平均延迟/吞吐量" :option="chartOption" />
      <el-card shadow="never">
        <template #header>压测记录</template>
        <ResultTable :rows="result.records" :columns="columns" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getPerformanceResults } from '../api/performance'
import ChartPanel from '../components/ChartPanel.vue'
import MetricCard from '../components/MetricCard.vue'
import ResultTable from '../components/ResultTable.vue'

const testType = ref('tpch')
const result = ref(null)
const columns = [
  { prop: 'threadCount', label: '线程数' },
  { prop: 'avgLatencyMs', label: '平均延迟(ms)' },
  { prop: 'throughput', label: '吞吐量' }
]

const chartOption = computed(() => {
  const data = result.value?.chartData || {}
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均延迟(ms)', '吞吐量'] },
    xAxis: { type: 'category', name: '线程数', data: data.xAxis || [] },
    yAxis: [
      { type: 'value', name: '延迟(ms)' },
      { type: 'value', name: '吞吐量', position: 'right' }
    ],
    series: [
      { name: '平均延迟(ms)', type: 'line', yAxisIndex: 0, data: data.latencySeries || [] },
      { name: '吞吐量', type: 'bar', yAxisIndex: 1, data: data.throughputSeries || [] }
    ]
  }
})

async function load() {
  const response = await getPerformanceResults({ testType: testType.value })
  result.value = response.data
}

onMounted(load)
</script>
