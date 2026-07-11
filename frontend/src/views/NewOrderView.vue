<template>
  <div class="page">
    <h2 class="page-title">TPC-C New-Order 事务</h2>
    <el-card shadow="never" class="content-card">
      <el-form :model="form" label-width="120px">
        <el-form-item label="仓库 ID"><el-input-number v-model="form.warehouseId" :min="1" /></el-form-item>
        <el-form-item label="地区 ID"><el-input-number v-model="form.districtId" :min="1" /></el-form-item>
        <el-form-item label="客户 ID"><el-input-number v-model="form.customerId" :min="1" /></el-form-item>
        <el-form-item label="商品 ID"><el-input-number v-model="form.items[0].itemId" :min="1" /></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="form.items[0].quantity" :min="1" /></el-form-item>
        <el-form-item><el-button type="primary" @click="submit">提交事务</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card v-if="result" shadow="never">
      <template #header>事务结果</template>
      <el-descriptions border :column="2">
        <el-descriptions-item label="事务 ID">{{ result.transactionId }}</el-descriptions-item>
        <el-descriptions-item label="状态"><StatusTag :status="result.status" /></el-descriptions-item>
        <el-descriptions-item label="订单 ID">{{ result.orderId }}</el-descriptions-item>
        <el-descriptions-item label="总金额">{{ result.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="耗时(ms)">{{ result.elapsedMs }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { createNewOrder } from '../api/tpcc'
import StatusTag from '../components/StatusTag.vue'

const result = ref(null)
const form = reactive({
  warehouseId: 1,
  districtId: 1,
  customerId: 1,
  items: [{ itemId: 1001, quantity: 1 }]
})

async function submit() {
  const response = await createNewOrder(form)
  result.value = response.data
}
</script>
