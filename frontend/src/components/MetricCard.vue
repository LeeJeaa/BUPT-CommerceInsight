<template>
  <el-card shadow="never" class="metric-card">
    <div class="metric-top">
      <div>
        <div class="metric-label">{{ label }}</div>
        <div class="metric-value">{{ value }}</div>
      </div>
      <div v-if="IconComponent" class="metric-icon">
        <el-icon><component :is="IconComponent" /></el-icon>
      </div>
    </div>
    <div v-if="hint" class="metric-hint">{{ hint }}</div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { Coin, DataBoard, Odometer, Tickets, Timer, TrendCharts, User } from '@element-plus/icons-vue'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  hint: { type: String, default: '' },
  icon: { type: String, default: '' }
})

const icons = {
  coin: Coin,
  dashboard: DataBoard,
  odometer: Odometer,
  tickets: Tickets,
  timer: Timer,
  trend: TrendCharts,
  user: User
}

const IconComponent = computed(() => icons[props.icon])
</script>

<style scoped>
.metric-card {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  background:
    radial-gradient(circle at 92% 10%, rgba(185, 149, 85, .18), transparent 38%),
    linear-gradient(135deg, rgba(255, 252, 244, .96), rgba(242, 232, 212, .74)) !important;
  cursor: var(--ci-ingot-cursor);
}
.metric-card::before {
  content: "";
  position: absolute;
  inset: 0;
  border-top: 3px solid rgba(185, 149, 85, .58);
}
.metric-card::after {
  content: "";
  position: absolute;
  right: -32px;
  bottom: -42px;
  width: 108px;
  height: 108px;
  border-radius: 999px;
  background: rgba(169, 128, 68, .1);
  transition: transform .24s ease;
}
.metric-card:hover::after {
  transform: scale(1.12);
}
.metric-top {
  position: relative;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}
.metric-label {
  color: var(--ci-muted);
  font-size: 13px;
  font-weight: 650;
}
.metric-value {
  margin-top: 8px;
  color: #342a20;
  font-size: 25px;
  font-weight: 820;
  line-height: 1.18;
}
.metric-icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  border-radius: 12px;
  color: #60441f;
  background:
    radial-gradient(circle at 30% 24%, rgba(255, 255, 255, .5), transparent 30%),
    linear-gradient(135deg, #e8d9b4, #b99555);
  box-shadow: inset 0 1px 3px rgba(255,255,255,.42), 0 9px 18px rgba(118, 86, 46, .14);
  transition: transform .22s ease, background .22s ease, box-shadow .22s ease;
}
.metric-card:hover .metric-icon {
  transform: rotate(-7deg) scale(1.06);
  box-shadow: inset 0 1px 4px rgba(255,255,255,.5), 0 12px 24px rgba(169, 128, 68, .22);
}
.metric-hint {
  position: relative;
  margin-top: 6px;
  color: var(--ci-muted);
  font-size: 12px;
}
</style>
