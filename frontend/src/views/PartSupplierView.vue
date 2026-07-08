<template>
  <div class="page">
    <h2 class="page-title">零部件供应查询</h2>
    <div class="toolbar">
      <el-input v-model="filters.keyword" placeholder="零部件/供应商关键词" style="width: 260px" clearable />
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-card shadow="never">
      <ResultTable :rows="rows" :columns="columns" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { queryPartSupplier } from '../api/query'
import ResultTable from '../components/ResultTable.vue'

const filters = reactive({ keyword: '' })
const rows = ref([])
const columns = [
  { prop: 'partKey', label: '零件 Key' },
  { prop: 'partName', label: '零件名称' },
  { prop: 'supplierName', label: '供应商' },
  { prop: 'nationName', label: '国家' },
  { prop: 'availQty', label: '可供数量' },
  { prop: 'supplyCost', label: '供应成本' }
]

async function load() {
  const response = await queryPartSupplier(filters)
  rows.value = response.data.records
}

onMounted(load)
</script>
