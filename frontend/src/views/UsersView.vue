<template>
  <div class="page">
    <h2 class="page-title">用户管理</h2>
    <div class="toolbar">
      <el-input v-model="filters.keyword" placeholder="账号/姓名" style="width: 220px" clearable />
      <el-select v-model="filters.status" placeholder="状态" style="width: 160px" clearable>
        <el-option label="待审批" value="pending" />
        <el-option label="已审批" value="approved" />
        <el-option label="已禁用" value="disabled" />
      </el-select>
      <el-button type="primary" @click="loadUsers">查询</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="users" border stripe>
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="role" label="角色" />
        <el-table-column label="状态">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="操作" width="190">
          <template #default="{ row }">
            <el-button size="small" type="success" :disabled="row.status === 'approved'" @click="approve(row)">审批</el-button>
            <el-button size="small" type="warning" :disabled="row.status === 'disabled'" @click="disable(row)">禁用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import StatusTag from '../components/StatusTag.vue'
import { approveUser, disableUser, listUsers } from '../api/user'

const filters = reactive({ keyword: '', status: '' })
const users = ref([])

async function loadUsers() {
  const response = await listUsers(filters)
  users.value = response.data.records
}

async function approve(row) {
  await approveUser(row.userId)
  row.status = 'approved'
  ElMessage.success('用户已审批')
}

async function disable(row) {
  await disableUser(row.userId)
  row.status = 'disabled'
  ElMessage.success('用户已禁用')
}

onMounted(loadUsers)
</script>
