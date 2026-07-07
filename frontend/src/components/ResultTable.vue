<template>
  <el-table :data="rows" border stripe class="full-width result-table">
    <el-table-column
      v-for="column in columns"
      :key="column.prop"
      :prop="column.prop"
      :label="column.label"
      :min-width="column.minWidth || 120"
    />
  </el-table>
</template>

<script setup>
defineProps({
  rows: { type: Array, default: () => [] },
  columns: { type: Array, required: true }
})
</script>

<style scoped>
.result-table {
  border-radius: 8px;
}
</style>
