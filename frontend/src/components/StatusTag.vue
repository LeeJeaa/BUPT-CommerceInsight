<template>
  <el-tag class="status-tag" :class="`status-${status || 'default'}`" :type="type" effect="light">{{ label }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, default: '' }
})

const type = computed(() => {
  if (['approved', 'success', 'committed', 'ready'].includes(props.status)) return 'success'
  if (['pending', 'running', 'mock-ready'].includes(props.status)) return 'warning'
  if (['disabled', 'failed'].includes(props.status)) return 'danger'
  return 'info'
})

const label = computed(() => {
  const labels = {
    approved: '已审批',
    pending: '待审批',
    disabled: '已禁用',
    success: '成功',
    running: '运行中',
    committed: '已提交',
    failed: '失败',
    ready: '就绪',
    'mock-ready': 'Mock 就绪'
  }
  return labels[props.status] || props.status || '未知'
})
</script>

<style scoped>
.status-tag {
  border: none;
  border-radius: 999px;
  font-weight: 700;
}
.status-approved,
.status-success,
.status-committed,
.status-ready {
  color: #4f6c63;
  background: rgba(136, 160, 150, .18);
}
.status-pending,
.status-running,
.status-mock-ready {
  color: #8a6d34;
  background: rgba(216, 190, 138, .22);
}
.status-disabled,
.status-failed {
  color: #9b5e5e;
  background: rgba(216, 167, 167, .24);
}
</style>
