<template>
  <div class="page">
    <h2 class="page-title">数据导出</h2>
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="tableName" placeholder="选择导出表" style="width: 240px">
          <el-option v-for="name in tableOptions" :key="name" :label="name" :value="name" />
        </el-select>
        <el-button type="primary" @click="download">导出 CSV</el-button>
      </div>
      <el-alert type="info" :closable="false" title="导出文件名使用表名，真实接口返回 CSV 文件流；Mock 模式返回演示 CSV。" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { exportTable } from '../api/importExport'

const tableOptions = ['orders', 'lineitem', 'partsupp', 'customer', 'supplier', 'part']
const tableName = ref('orders')

async function download() {
  const blob = await exportTable(tableName.value)
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${tableName.value}.csv`
  link.click()
  URL.revokeObjectURL(url)
}
</script>
