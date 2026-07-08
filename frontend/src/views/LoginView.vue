<template>
  <div class="auth-page">
    <div class="auth-orb orb-one"></div>
    <div class="auth-orb orb-two"></div>
    <el-card class="auth-card" shadow="never">
      <div class="auth-badge">
        <el-icon><DataBoard /></el-icon>
      </div>
      <h1>TPC CommerceInsight</h1>
      <p class="muted">柔和、清晰、可演示的电商 Benchmark 分析系统</p>
      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="admin 或 user1" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="admin123" />
        </el-form-item>
        <el-button type="primary" class="full-width" :loading="loading" @click="submit">进入系统</el-button>
      </el-form>
      <div class="auth-footer">
        <span>没有账号？</span>
        <router-link to="/register">注册并等待审批</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { DataBoard } from '@element-plus/icons-vue'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123'
})

async function submit() {
  loading.value = true
  try {
    const response = await login(form)
    auth.setSession(response.data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 18%, rgba(185, 149, 85, .18), transparent 32%),
    radial-gradient(circle at 82% 22%, rgba(135, 146, 122, .14), transparent 30%),
    radial-gradient(circle at 68% 82%, rgba(185, 139, 120, .12), transparent 28%),
    repeating-linear-gradient(90deg, rgba(118, 86, 46, .018) 0 1px, transparent 1px 42px),
    linear-gradient(135deg, #fbf6ea, #eadcc6);
}
.auth-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(.2px);
  animation: floaty 6s ease-in-out infinite;
}
.orb-one {
  width: 110px;
  height: 110px;
  left: 14%;
  top: 18%;
  background: rgba(185, 149, 85, .18);
}
.orb-two {
  width: 150px;
  height: 150px;
  right: 12%;
  bottom: 16%;
  background: rgba(135, 146, 122, .14);
  animation-delay: -2s;
}
.auth-card {
  width: min(430px, 100%);
  padding: 8px;
  border-radius: 8px;
  text-align: center;
  background:
    radial-gradient(circle at 92% 8%, rgba(185, 149, 85, .12), transparent 28%),
    rgba(255, 252, 244, .9) !important;
}
.auth-badge {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  margin: 4px auto 14px;
  border-radius: 16px;
  color: #60441f;
  background:
    radial-gradient(circle at 34% 28%, #ead9a6 0 18%, transparent 19%),
    linear-gradient(135deg, #e8d9b4, #b99555 48%, #76562e);
  box-shadow: 0 12px 26px rgba(118, 86, 46, .18);
}
h1 {
  margin: 0 0 8px;
  color: #342a20;
  font-size: 27px;
}
.auth-footer {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-top: 16px;
}
@keyframes floaty {
  50% {
    transform: translateY(-12px);
  }
}
</style>
