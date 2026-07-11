<template>
  <div class="page">
    <h2 class="page-title">数据导入</h2>
    <el-card shadow="never" class="content-card">
      <div class="toolbar">
        <el-select v-model="tableName" placeholder="选择导入表" style="width: 220px">
          <el-option label="orders" value="orders" />
          <el-option label="lineitem" value="lineitem" />
        </el-select>
        <el-upload :auto-upload="false" :limit="1" :on-change="onFileChange" :on-remove="onFileRemove">
          <el-button>选择文件</el-button>
        </el-upload>
        <el-button type="primary" :loading="loading" @click="submit">发起导入</el-button>
      </div>
      <el-alert
        type="info"
        :closable="false"
        title="Web 导入仅用于系统演示，只支持 orders 与 lineitem；正式 TPC-H 数据由 D 负责通过 dbgen + COPY 完成。"
      />
    </el-card>

    <div v-if="task" class="metric-grid">
      <MetricCard label="最终状态" :value="task.status || '-'" />
      <MetricCard label="总行数" :value="task.totalRows ?? 0" />
      <MetricCard label="成功行数" :value="task.successRows ?? 0" />
      <MetricCard label="失败行数" :value="task.failedRows ?? 0" />
      <MetricCard label="耗时(ms)" :value="task.elapsedMs ?? 0" />
    </div>

    <el-card v-if="task" shadow="never" class="content-card">
      <template #header>导入任务</template>
      <el-descriptions border :column="2">
        <el-descriptions-item label="任务编号">{{ task.taskId }}</el-descriptions-item>
        <el-descriptions-item label="表名">{{ task.tableName }}</el-descriptions-item>
        <el-descriptions-item label="文件名">{{ task.fileName }}</el-descriptions-item>
        <el-descriptions-item label="状态"><StatusTag :status="task.status" /></el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ task.startedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ task.endedAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="task" shadow="never">
      <template #header>错误日志</template>
      <ResultTable v-if="errors.length" :rows="errors" :columns="errorColumns" />
      <el-empty v-else description="暂无错误日志" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
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
  if (!file.value) {
    ElMessage.warning('请先选择导入文件')
    return
  }
  loading.value = true
  errors.value = []
  try {
    const response = await createImportTask({ tableName: tableName.value, file: file.value })
    task.value = response.data
    const errorResponse = await getImportErrors(response.data.taskId, { pageNo: 1, pageSize: 20 })
    errors.value = errorResponse.data.records || []
  } finally {
    loading.value = false
  }
}
</script>
