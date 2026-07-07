<template>
  <div class="page">
    <h2 class="page-title">客户查询</h2>
    <div class="toolbar">
      <el-input v-model="filters.keyword" placeholder="客户姓名关键词" style="width: 220px" clearable />
      <el-select v-model="filters.nationName" placeholder="国家" style="width: 180px" clearable>
        <el-option label="CHINA" value="CHINA" />
        <el-option label="JAPAN" value="JAPAN" />
        <el-option label="INDIA" value="INDIA" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-card shadow="never">
      <ResultTable :rows="rows" :columns="columns" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { queryCustomers } from '../api/query'
import ResultTable from '../components/ResultTable.vue'

const filters = reactive({ keyword: '', nationName: '', pageNo: 1, pageSize: 20 })
const rows = ref([])
const columns = [
  { prop: 'customerKey', label: '客户 Key' },
  { prop: 'customerName', label: '客户名称', minWidth: 180 },
  { prop: 'nationName', label: '国家' },
  { prop: 'accountBalance', label: '账户余额' },
  { prop: 'marketSegment', label: '市场领域' }
]

async function load() {
  const response = await queryCustomers(filters)
  rows.value = response.data.records
}

onMounted(load)
</script>
