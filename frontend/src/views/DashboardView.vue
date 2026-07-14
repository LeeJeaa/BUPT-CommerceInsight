<template>
  <div class="page">
    <h2 class="page-title">系统总览</h2>
    <section class="dashboard-hero">
      <div>
        <div class="hero-kicker">Commerce Data Console</div>
        <h3>TPC Benchmark 电商数据驾驶舱</h3>
        <p>整合客户、订单、零部件、事务与性能分析，支持课程验收中的前端演示、Mock 展示和后端联调。</p>
      </div>
      <div class="hero-stats">
        <div>
          <strong>{{ dashboard.tableCount }}</strong>
          <span>核心表</span>
        </div>
        <div>
          <strong>{{ dashboard.dataScale }}</strong>
          <span>数据规模</span>
        </div>
      </div>
    </section>
    <div class="metric-grid">
      <MetricCard label="数据库" :value="dashboard.databaseName" :hint="dashboard.dockerStatus" icon="dashboard" />
      <MetricCard label="表数量" :value="dashboard.tableCount" hint="TPC-H + TPC-C + 应用辅助表" icon="tickets" />
      <MetricCard label="数据规模" :value="dashboard.dataScale" hint="当前数据库实时统计" icon="coin" />
      <MetricCard label="核心流程" value="12 步" hint="按冻结验收顺序演示" icon="trend" />
    </div>
    <div class="chart-grid">
      <el-card shadow="never" class="content-card">
        <template #header>核心表行数</template>
        <el-table v-loading="loading" :data="dashboard.rowCounts" border>
          <el-table-column prop="tableName" label="表名" />
          <el-table-column prop="rowCount" label="行数" />
        </el-table>
      </el-card>
      <el-card shadow="never" class="content-card">
        <template #header>模块状态</template>
        <el-table :data="dashboard.modules" border>
          <el-table-column prop="name" label="模块" />
          <el-table-column label="状态">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { queryDashboardSummary } from '../api/dashboard'
import MetricCard from '../components/MetricCard.vue'
import StatusTag from '../components/StatusTag.vue'
import { dashboardMock } from '../mock/dashboard.mock'

const dashboard = ref(dashboardMock)
const loading = ref(false)

async function loadDashboard() {
  loading.value = true
  try {
    const response = await queryDashboardSummary()
    dashboard.value = response.data
  } catch (error) {
    if (error.response?.status !== 401) {
      console.error('Dashboard load failed', error)
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<style scoped>
.dashboard-hero {
  position: relative;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
  margin-bottom: 18px;
  padding: 24px 26px;
  overflow: hidden;
  border: 1px solid rgba(148, 112, 65, .24);
  border-radius: 8px;
  color: #fff8e8;
  background:
    radial-gradient(circle at 12% 10%, rgba(226, 205, 151, .22), transparent 28%),
    radial-gradient(circle at 88% 20%, rgba(135, 146, 122, .18), transparent 32%),
    repeating-linear-gradient(135deg, rgba(255, 248, 226, .04) 0 1px, transparent 1px 18px),
    linear-gradient(135deg, #6d512f 0%, #987545 48%, #4d3b2b 100%);
  box-shadow: 0 18px 44px rgba(72, 52, 31, .2);
  animation: fade-up .36s ease both;
}

.dashboard-hero::after {
  content: "";
  position: absolute;
  right: -54px;
  bottom: -74px;
  width: 210px;
  height: 210px;
  border-radius: 999px;
  background: rgba(226, 205, 151, .12);
  box-shadow: inset 0 0 0 1px rgba(248, 239, 217, .14);
}

.hero-kicker {
  position: relative;
  z-index: 1;
  margin-bottom: 8px;
  color: rgba(248, 239, 217, .78);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: .8px;
  text-transform: uppercase;
}

.dashboard-hero h3 {
  position: relative;
  z-index: 1;
  margin: 0;
  font-size: 26px;
  line-height: 1.25;
}

.dashboard-hero p {
  position: relative;
  z-index: 1;
  max-width: 660px;
  margin: 10px 0 0;
  color: rgba(248, 239, 217, .82);
  line-height: 1.7;
}

.hero-stats {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 12px;
}

.hero-stats div {
  min-width: 118px;
  padding: 14px 16px;
  border: 1px solid rgba(248, 239, 217, .22);
  border-radius: 8px;
  background: rgba(248, 239, 217, .12);
  backdrop-filter: blur(10px);
}

.hero-stats strong {
  display: block;
  font-size: 22px;
}

.hero-stats span {
  display: block;
  margin-top: 4px;
  color: rgba(248, 239, 217, .78);
  font-size: 12px;
}
</style>
