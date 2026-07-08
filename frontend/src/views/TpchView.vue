<template>
  <div class="page">
    <h2 class="page-title">TPC-H 查询分析</h2>
    <el-tabs v-model="active" type="border-card" @tab-change="run">
      <el-tab-pane label="Q1 定价汇总" name="q1">
        <div class="toolbar">
          <el-date-picker v-model="forms.q1.shipDate" value-format="YYYY-MM-DD" type="date" placeholder="shipDate" />
          <el-button type="primary" @click="run">执行 Q1</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="Q5 本地供应商收入" name="q5">
        <div class="toolbar">
          <el-select v-model="forms.q5.regionName" style="width: 160px">
            <el-option label="AFRICA" value="AFRICA" />
            <el-option label="ASIA" value="ASIA" />
            <el-option label="EUROPE" value="EUROPE" />
          </el-select>
          <el-date-picker v-model="forms.q5.startDate" value-format="YYYY-MM-DD" type="date" placeholder="startDate" />
          <el-date-picker v-model="forms.q5.endDate" value-format="YYYY-MM-DD" type="date" placeholder="endDate" />
          <el-button type="primary" @click="run">执行 Q5</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="Q12 运送方式" name="q12">
        <div class="toolbar">
          <el-select v-model="forms.q12.shipMode1" style="width: 140px"><el-option label="MAIL" value="MAIL" /></el-select>
          <el-select v-model="forms.q12.shipMode2" style="width: 140px"><el-option label="SHIP" value="SHIP" /></el-select>
          <el-date-picker v-model="forms.q12.startDate" value-format="YYYY-MM-DD" type="date" placeholder="startDate" />
          <el-date-picker v-model="forms.q12.endDate" value-format="YYYY-MM-DD" type="date" placeholder="endDate" />
          <el-button type="primary" @click="run">执行 Q12</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="Q14 促销效果" name="q14">
        <div class="toolbar">
          <el-date-picker v-model="forms.q14.month" value-format="YYYY-MM-DD" type="month" placeholder="month" />
          <el-button type="primary" @click="run">执行 Q14</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>

    <div v-if="result" class="metric-grid page-section">
      <MetricCard label="查询名称" :value="result.queryName" icon="trend" />
      <MetricCard label="耗时(ms)" :value="result.elapsedMs" icon="timer" />
      <MetricCard label="返回行数" :value="result.rowCount" icon="dashboard" />
    </div>

    <div v-if="result" class="chart-grid">
      <el-card shadow="never">
        <template #header>查询结果</template>
        <ResultTable :rows="result.records" :columns="columns" />
      </el-card>
      <ChartPanel :title="`${active.toUpperCase()} 图表`" :option="chartOption" />
    </div>

    <el-card v-if="result?.explainPlan" class="content-card" shadow="never">
      <template #header>EXPLAIN 计划摘要</template>
      <pre>{{ result.explainPlan }}</pre>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { runQ1, runQ5, runQ12, runQ14 } from '../api/tpch'
import ChartPanel from '../components/ChartPanel.vue'
import MetricCard from '../components/MetricCard.vue'
import ResultTable from '../components/ResultTable.vue'

const active = ref('q5')
const result = ref(null)
const forms = reactive({
  q1: { shipDate: '2020-12-31' },
  q5: { regionName: 'AFRICA', startDate: '2020-01-01', endDate: '2021-01-01' },
  q12: { shipMode1: 'MAIL', shipMode2: 'SHIP', startDate: '2020-01-01', endDate: '2021-01-01' },
  q14: { month: '2020-09-01' }
})

const columnMap = {
  q1: [
    { prop: 'returnFlag', label: '退货标志' },
    { prop: 'lineStatus', label: '明细状态' },
    { prop: 'sumQuantity', label: '数量合计' },
    { prop: 'sumCharge', label: '总费用' },
    { prop: 'avgPrice', label: '平均单价' },
    { prop: 'countOrder', label: '订单行数' }
  ],
  q5: [
    { prop: 'nationName', label: '国家' },
    { prop: 'revenue', label: '收入' }
  ],
  q12: [
    { prop: 'shipMode', label: '运输方式' },
    { prop: 'highLineCount', label: '高优先级行数' },
    { prop: 'lowLineCount', label: '低优先级行数' }
  ],
  q14: [
    { prop: 'promoRevenuePercent', label: '促销收入占比(%)' }
  ]
}

const columns = computed(() => columnMap[active.value])

const chartOption = computed(() => {
  if (!result.value) return {}
  const data = result.value.chartData || {}
  if (active.value === 'q12') {
    return {
      tooltip: { trigger: 'axis' },
      legend: { data: ['高优先级', '低优先级'] },
      xAxis: { type: 'category', data: data.xAxis || [] },
      yAxis: { type: 'value' },
      series: [
        { name: '高优先级', type: 'bar', stack: 'total', data: data.highSeries || data.highPrioritySeries || [] },
        { name: '低优先级', type: 'bar', stack: 'total', data: data.lowSeries || data.lowPrioritySeries || [] }
      ]
    }
  }
  if (active.value === 'q14') {
    return {
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: '62%', data: data.pieData || [] }]
    }
  }
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.xAxis || [] },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: data.series || [] }]
  }
})

async function run() {
  const runners = { q1: runQ1, q5: runQ5, q12: runQ12, q14: runQ14 }
  const response = await runners[active.value](forms[active.value])
  result.value = response.data
}

onMounted(run)
</script>

<style scoped>
.page-section {
  margin-top: 16px;
}
pre {
  margin: 0;
  white-space: pre-wrap;
  color: #475467;
}
</style>
