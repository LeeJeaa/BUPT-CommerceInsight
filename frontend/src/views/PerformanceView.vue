<template>
  <div class="page">
    <h2 class="page-title">性能分析</h2>
    <div class="toolbar">
      <el-select v-model="testType" style="width: 220px" @change="load">
        <el-option label="TPC-H 并发查询" value="tpch" />
        <el-option label="TPC-C 并发事务" value="tpcc" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="load">刷新结果</el-button>
    </div>

    <el-alert v-if="error" class="content-card" type="error" :closable="false" :title="error" />

    <el-card v-if="empty" v-loading="loading" shadow="never" class="content-card">
      <el-empty description="暂无结果，请先运行压测" />
    </el-card>

    <template v-else>
      <div v-if="result" v-loading="loading" class="metric-grid">
        <MetricCard label="线程数" :value="result.threadCount ?? '-'" icon="odometer" />
        <MetricCard label="总请求" :value="result.totalRequests ?? 0" icon="dashboard" />
        <MetricCard label="成功/失败" :value="`${result.successCount ?? 0}/${result.failCount ?? 0}`" icon="tickets" />
        <MetricCard label="吞吐量" :value="result.throughput ?? '-'" hint="QPS/TPS" icon="trend" />
        <MetricCard label="平均延迟(ms)" :value="result.avgLatencyMs ?? '-'" icon="timer" />
        <MetricCard label="最大延迟(ms)" :value="result.maxLatencyMs ?? '-'" icon="timer" />
      </div>
      <div v-if="result" class="chart-grid">
        <ChartPanel title="线程数-平均延迟/吞吐量" :option="chartOption" />
        <el-card shadow="never">
          <template #header>压测记录</template>
          <ResultTable v-if="result.records?.length" :rows="result.records" :columns="columns" />
          <el-empty v-else description="暂无压测记录" />
        </el-card>
      </div>
    </template>
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
const loading = ref(false)
const error = ref('')
const columns = [
  { prop: 'threadCount', label: '线程数' },
  { prop: 'avgLatencyMs', label: '平均延迟(ms)' },
  { prop: 'throughput', label: '吞吐量' }
]

const hasChartData = computed(() => {
  const chartData = result.value?.chartData || {}
  return Boolean(chartData.xAxis?.length && chartData.latencySeries?.length && chartData.throughputSeries?.length)
})

const empty = computed(() => {
  if (loading.value || error.value) return false
  if (!result.value) return true
  return !(result.value.records?.length || hasChartData.value)
})

const chartOption = computed(() => {
  const chartData = result.value?.chartData || {}
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均延迟(ms)', '吞吐量'] },
    xAxis: { type: 'category', name: '线程数', data: chartData.xAxis || [] },
    yAxis: [
      { type: 'value', name: '延迟(ms)' },
      { type: 'value', name: '吞吐量', position: 'right' }
    ],
    series: [
      { name: '平均延迟(ms)', type: 'line', yAxisIndex: 0, data: chartData.latencySeries || [] },
      { name: '吞吐量', type: 'bar', yAxisIndex: 1, data: chartData.throughputSeries || [] }
    ]
  }
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const response = await getPerformanceResults({ testType: testType.value })
    result.value = response.data
  } catch (err) {
    result.value = null
    if (err.response?.status !== 404) {
      error.value = err.response?.data?.message || err.message || '性能结果加载失败'
    }
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
