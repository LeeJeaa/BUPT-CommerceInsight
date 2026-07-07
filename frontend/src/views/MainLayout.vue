<template>
  <el-container class="app-shell">
    <el-aside width="258px" class="sidebar">
      <div class="brand">
        <div class="brand-mark">
          <el-icon><DataBoard /></el-icon>
        </div>
        <div>
          <div class="brand-title">CommerceInsight</div>
          <div class="brand-subtitle">TPC Benchmark Lab</div>
        </div>
      </div>
      <el-menu router :default-active="$route.path" class="side-menu">
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>系统总览</span>
        </el-menu-item>
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/users" :disabled="auth.role !== 'admin'">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="data">
          <template #title>
            <el-icon><FolderOpened /></el-icon>
            <span>数据管理</span>
          </template>
          <el-menu-item index="/import" :disabled="auth.role !== 'admin'">
            <el-icon><Upload /></el-icon>
            <span>数据导入</span>
          </el-menu-item>
          <el-menu-item index="/export" :disabled="auth.role !== 'admin'">
            <el-icon><Download /></el-icon>
            <span>数据导出</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="query">
          <template #title>
            <el-icon><Search /></el-icon>
            <span>业务查询</span>
          </template>
          <el-menu-item index="/query/customers">
            <el-icon><User /></el-icon>
            <span>客户查询</span>
          </el-menu-item>
          <el-menu-item index="/query/order-revenue">
            <el-icon><Coin /></el-icon>
            <span>订单收入</span>
          </el-menu-item>
          <el-menu-item index="/query/part-supplier">
            <el-icon><Box /></el-icon>
            <span>零部件供应</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="analysis">
          <template #title>
            <el-icon><TrendCharts /></el-icon>
            <span>业务分析</span>
          </template>
          <el-menu-item index="/tpch">
            <el-icon><TrendCharts /></el-icon>
            <span>TPC-H 分析</span>
          </el-menu-item>
          <el-menu-item index="/tpcc/new-order">
            <el-icon><Tickets /></el-icon>
            <span>New-Order 事务</span>
          </el-menu-item>
          <el-menu-item index="/tpcc/payment">
            <el-icon><Wallet /></el-icon>
            <span>Payment 事务</span>
          </el-menu-item>
          <el-menu-item index="/performance">
            <el-icon><Odometer /></el-icon>
            <span>性能分析</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="topbar-left">
          <div class="sparkle">✦</div>
          <div>
            <div class="welcome">欢迎回来，{{ auth.username }}</div>
          </div>
        </div>
        <div class="topbar-right">
          <el-tag class="role-tag">{{ auth.role }}</el-tag>
          <el-button plain :icon="SwitchButton" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main-area">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import {
  Box,
  Coin,
  DataBoard,
  Download,
  FolderOpened,
  Odometer,
  Search,
  Setting,
  SwitchButton,
  Tickets,
  TrendCharts,
  Upload,
  User,
  Wallet
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
}
.sidebar {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 0%, rgba(185, 149, 85, .2), transparent 34%),
    radial-gradient(circle at 84% 30%, rgba(118, 86, 46, .2), transparent 32%),
    linear-gradient(180deg, #433629 0%, #302b25 46%, #211f1c 100%);
  box-shadow: 12px 0 34px rgba(72, 52, 31, .18);
}
.sidebar::after {
  content: "";
  position: absolute;
  right: -60px;
  bottom: -40px;
  width: 160px;
  height: 160px;
  border-radius: 999px;
  background: rgba(185, 149, 85, .14);
  box-shadow: 0 0 70px rgba(169, 128, 68, .18);
}
.brand {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 18px;
  align-items: center;
  justify-content: center;
  min-height: 78px;
  padding: 18px 16px;
  color: #f8efd9;
  text-align: left;
  cursor: var(--ci-ingot-cursor);
}
.brand-mark {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 999px;
  color: #5d421f;
  background:
    radial-gradient(circle at 35% 28%, #f5e6b0 0 18%, transparent 19%),
    linear-gradient(135deg, #e7cf8f 0%, #b99555 48%, #76562e 100%);
  box-shadow:
    inset 0 2px 5px rgba(255, 255, 255, .5),
    0 12px 26px rgba(29, 21, 14, .32);
  transition: transform .22s ease, box-shadow .22s ease;
}
.brand:hover .brand-mark {
  transform: translateY(-1px) rotate(-5deg);
  box-shadow:
    inset 0 2px 5px rgba(255, 255, 255, .56),
    0 15px 34px rgba(169, 128, 68, .22);
}
.brand-title {
  font-size: 16px;
  font-weight: 850;
  letter-spacing: .3px;
}
.brand-subtitle {
  margin-top: 2px;
  color: rgba(238, 222, 184, .76);
  font-size: 12px;
}
.side-menu {
  position: relative;
  z-index: 1;
  border-right: 0;
  --el-menu-bg-color: transparent;
  --el-menu-hover-bg-color: rgba(185, 149, 85, .13);
  --el-menu-active-color: #f8efd9;
  --el-menu-text-color: rgba(248, 239, 217, .88);
  background: transparent !important;
}
.side-menu :deep(.el-menu),
.side-menu :deep(.el-menu--inline) {
  background: transparent !important;
}
.side-menu :deep(.el-sub-menu) {
  background: transparent !important;
}
.side-menu :deep(.el-menu-item),
.side-menu :deep(.el-sub-menu__title) {
  height: 46px;
  margin: 4px 12px;
  border-radius: 8px;
  color: rgba(248, 239, 217, .88);
  background: rgba(248, 239, 217, .045);
  transition: background .18s ease, color .18s ease, transform .18s ease, box-shadow .18s ease;
}
.side-menu :deep(.el-sub-menu .el-menu-item) {
  margin-left: 22px;
  color: rgba(238, 222, 184, .78);
  background: rgba(29, 24, 19, .2);
}
.side-menu :deep(.el-menu-item:hover),
.side-menu :deep(.el-sub-menu__title:hover) {
  color: #fbf1d8;
  background: rgba(185, 149, 85, .14);
  transform: translateX(2px);
  box-shadow: inset 3px 0 0 rgba(185, 149, 85, .56);
}
.side-menu :deep(.el-menu-item.is-active) {
  color: #4b3825;
  background:
    linear-gradient(135deg, rgba(248, 239, 217, .96), rgba(205, 180, 132, .88));
  box-shadow: 0 12px 24px rgba(27, 20, 14, .2), inset 0 1px 0 rgba(255,255,255,.52);
}
.side-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  color: #4b3825;
  background: linear-gradient(135deg, rgba(248, 239, 217, .96), rgba(205, 180, 132, .88));
}
.side-menu :deep(.el-sub-menu__icon-arrow) {
  color: rgba(238, 222, 184, .8);
}
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 66px;
  border-bottom: 1px solid rgba(148, 112, 65, .16);
  background:
    linear-gradient(90deg, rgba(255, 252, 244, .88), rgba(245, 237, 222, .76));
  backdrop-filter: blur(16px);
}
.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.sparkle {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  color: #76562e;
  background: rgba(185, 149, 85, .16);
  box-shadow: inset 0 0 0 1px rgba(148, 112, 65, .16);
}
.welcome {
  color: #3c3428;
  font-weight: 780;
}
.role-tag {
  border: 0;
  border-radius: 999px;
  color: #76562e;
  background: rgba(185, 149, 85, .14);
  font-weight: 750;
}
.main-area {
  padding: 0;
}
</style>
