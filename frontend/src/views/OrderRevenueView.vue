<template>
  <div class="page">
    <h2 class="page-title">订单收入查询</h2>
    <div class="toolbar">
      <el-date-picker v-model="filters.startDate" value-format="YYYY-MM-DD" type="date" placeholder="开始日期" />
      <el-date-picker v-model="filters.endDate" value-format="YYYY-MM-DD" type="date" placeholder="结束日期" />
      <el-button type="primary" :loading="loading" @click="load">查询</el-button>
    </div>
    <el-alert v-if="error" class="content-card" type="error" :closable="false" :title="error" />
    <div class="chart-grid">
      <el-card v-loading="loading" shadow="never">
        <template #header>订单收入结果</template>
        <ResultTable v-if="rows.length" :rows="rows" :columns="columns" />
        <el-empty v-else-if="!loading && !error" description="暂无订单收入结果" />
      </el-card>
      <ChartPanel v-if="rows.length" title="订单收入趋势" :option="chartOption" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { queryOrderRevenue } from '../api/query'
import ChartPanel from '../components/ChartPanel.vue'
import ResultTable from '../components/ResultTable.vue'

const filters = reactive({ startDate: '2019-01-01', endDate: '2019-12-31', pageNo: 1, pageSize: 20 })
const rows = ref([])
const loading = ref(false)
const error = ref('')
const columns = [
  { prop: 'orderKey', label: '订单 Key' },
  { prop: 'orderDate', label: '订单日期' },
  { prop: 'customerName', label: '客户名称', minWidth: 180 },
  { prop: 'revenue', label: '收入' }
]

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: rows.value.map((item) => item.orderDate) },
  yAxis: { type: 'value', name: '收入' },
  series: [{ type: 'bar', data: rows.value.map((item) => item.revenue), name: '收入' }]
}))

async function load() {
  loading.value = true
  error.value = ''
  try {
    const response = await queryOrderRevenue(filters)
    rows.value = response.data.records || []
  } catch (err) {
    rows.value = []
    error.value = err.response?.data?.message || err.message || '订单收入查询失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
