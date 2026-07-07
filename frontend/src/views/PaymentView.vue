<template>
  <div class="page">
    <h2 class="page-title">TPC-C Payment 事务</h2>
    <el-card shadow="never" class="content-card">
      <el-form :model="form" label-width="120px">
        <el-form-item label="仓库 ID"><el-input-number v-model="form.warehouseId" :min="1" /></el-form-item>
        <el-form-item label="地区 ID"><el-input-number v-model="form.districtId" :min="1" /></el-form-item>
        <el-form-item label="客户 ID"><el-input-number v-model="form.customerId" :min="1" /></el-form-item>
        <el-form-item label="支付金额"><el-input-number v-model="form.paymentAmount" :min="0.01" :precision="2" /></el-form-item>
        <el-form-item><el-button type="primary" @click="submit">提交付款</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card v-if="result" shadow="never">
      <template #header>事务结果</template>
      <el-descriptions border :column="2">
        <el-descriptions-item label="事务 ID">{{ result.transactionId }}</el-descriptions-item>
        <el-descriptions-item label="状态"><StatusTag :status="result.status" /></el-descriptions-item>
        <el-descriptions-item label="客户 ID">{{ result.customerId }}</el-descriptions-item>
        <el-descriptions-item label="新余额">{{ result.newBalance }}</el-descriptions-item>
        <el-descriptions-item label="耗时(ms)">{{ result.elapsedMs }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { createPayment } from '../api/tpcc'
import StatusTag from '../components/StatusTag.vue'

const result = ref(null)
const form = reactive({
  warehouseId: 1,
  districtId: 1,
  customerId: 1,
  paymentAmount: 25
})

async function submit() {
  const response = await createPayment(form)
  result.value = response.data
}
</script>
