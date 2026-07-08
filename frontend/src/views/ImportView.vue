<template>
  <div class="page">
    <h2 class="page-title">数据导入</h2>
    <el-card shadow="never" class="content-card">
      <div class="toolbar">
        <el-select v-model="tableName" placeholder="选择导入表" style="width: 220px">
          <el-option label="orders" value="orders" />
          <el-option label="lineitem" value="lineitem" />
          <el-option label="partsupp" value="partsupp" />
        </el-select>
        <el-upload :auto-upload="false" :limit="1" :on-change="onFileChange" :on-remove="onFileRemove">
          <el-button>选择文件</el-button>
        </el-upload>
        <el-button type="primary" :loading="loading" @click="submit">发起导入</el-button>
      </div>
      <el-alert
        type="info"
        :closable="false"
        title="系统演示导入由 B/C 实现，正式 dbgen + COPY 导入由 D 实现。"
      />
    </el-card>

    <div v-if="task" class="metric-grid">
      <MetricCard label="总行数" :value="task.totalRows" />
      <MetricCard label="成功行数" :value="task.successRows" />
      <MetricCard label="失败行数" :value="task.failedRows" />
      <MetricCard label="耗时(ms)" :value="task.elapsedMs" />
    </div>

    <el-card v-if="task" shadow="never" class="content-card">
      <template #header>导入任务</template>
      <el-descriptions border :column="2">
        <el-descriptions-item label="任务编号">{{ task.taskId }}</el-descriptions-item>
        <el-descriptions-item label="表名">{{ task.tableName }}</el-descriptions-item>
        <el-descriptions-item label="文件名">{{ task.fileName }}</el-descriptions-item>
        <el-descriptions-item label="状态"><StatusTag :status="task.status" /></el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="errors.length" shadow="never">
      <template #header>错误日志</template>
      <ResultTable :rows="errors" :columns="errorColumns" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { createImportTask, getImportErrors } from '../api/importExport'
import MetricCard from '../components/MetricCard.vue'
import ResultTable from '../components/ResultTable.vue'
import StatusTag from '../components/StatusTag.vue'

const tableName = ref('orders')
const file = ref(null)
const loading = ref(false)
const task = ref(null)
const errors = ref([])

const errorColumns = [
  { prop: 'lineNumber', label: '行号' },
  { prop: 'fieldName', label: '字段名' },
  { prop: 'fieldValue', label: '原始值' },
  { prop: 'errorReason', label: '错误原因', minWidth: 180 }
]

function onFileChange(uploadFile) {
  file.value = uploadFile.raw
}

function onFileRemove() {
  file.value = null
}

async function submit() {
  loading.value = true
  try {
    const response = await createImportTask({ tableName: tableName.value, file: file.value })
    task.value = response.data
    const errorResponse = await getImportErrors(response.data.taskId, { pageNo: 1, pageSize: 20 })
    errors.value = errorResponse.data.records
  } finally {
    loading.value = false
  }
}
</script>
