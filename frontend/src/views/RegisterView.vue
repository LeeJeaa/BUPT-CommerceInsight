<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="never">
      <div class="auth-badge">
        <el-icon><User /></el-icon>
      </div>
      <h1>用户注册</h1>
      <p class="muted">普通用户注册后需管理员审批。</p>
      <el-form :model="form" label-position="top">
        <el-form-item label="账号"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-button type="primary" class="full-width" :loading="loading" @click="submit">提交注册</el-button>
      </el-form>
      <el-alert v-if="result" class="result" type="success" :closable="false" :title="`注册成功，状态：${result.status}`" />
      <div class="auth-footer">
        <router-link to="/login">返回登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { User } from '@element-plus/icons-vue'
import { reactive, ref } from 'vue'
import { register } from '../api/auth'

const loading = ref(false)
const result = ref(null)
const form = reactive({
  username: 'alice',
  password: '123456',
  realName: 'Alice',
  email: 'alice@example.com'
})

async function submit() {
  loading.value = true
  try {
    const response = await register(form)
    result.value = response.data
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at 20% 20%, rgba(185, 149, 85, .18), transparent 30%),
    radial-gradient(circle at 82% 76%, rgba(135, 146, 122, .14), transparent 30%),
    repeating-linear-gradient(90deg, rgba(118, 86, 46, .018) 0 1px, transparent 1px 42px),
    linear-gradient(135deg, #fbf6ea, #eadcc6);
}
.auth-card {
  width: min(450px, 100%);
  padding: 8px;
  border-radius: 8px;
  background:
    radial-gradient(circle at 92% 8%, rgba(185, 149, 85, .12), transparent 28%),
    rgba(255, 252, 244, .9) !important;
}
.auth-badge {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  border-radius: 15px;
  color: #60441f;
  background:
    radial-gradient(circle at 34% 28%, #ead9a6 0 18%, transparent 19%),
    linear-gradient(135deg, #e8d9b4, #b99555 48%, #76562e);
  box-shadow: 0 12px 26px rgba(118, 86, 46, .16);
}
h1 {
  margin: 0 0 8px;
  color: #342a20;
}
.result {
  margin-top: 14px;
}
.auth-footer {
  text-align: center;
  margin-top: 16px;
}
</style>
