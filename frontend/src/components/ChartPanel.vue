<template>
  <el-card shadow="never" class="chart-card">
    <template #header>
      <div class="chart-header">
        <span>{{ title }}</span>
        <span class="chart-dot"></span>
      </div>
    </template>
    <div ref="chartEl" class="chart"></div>
  </el-card>
</template>

<script setup>
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  option: { type: Object, required: true }
})

const chartEl = ref()
let chart = null
const morandiColors = ['#B99555', '#87927A', '#B98B78', '#9C8A6B', '#7E8A8D', '#D6C39D']
const themedOption = computed(() => ({
  color: props.option.color || morandiColors,
  grid: props.option.grid || { top: 42, right: 28, bottom: 36, left: 48 },
  ...props.option
}))

function renderChart() {
  if (!chartEl.value) return
  if (!chart) {
    chart = echarts.init(chartEl.value)
  }
  chart.setOption(themedOption.value, true)
}

function resizeChart() {
  chart?.resize()
}

onMounted(async () => {
  await nextTick()
  renderChart()
  window.addEventListener('resize', resizeChart)
})

watch(themedOption, renderChart, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
})
</script>

<style scoped>
.chart-card {
  border-radius: 8px;
  cursor: var(--ci-ingot-cursor);
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chart-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: radial-gradient(circle at 35% 30%, #ead9a6 0 18%, transparent 19%), linear-gradient(135deg, var(--ci-gold), var(--ci-amber));
  box-shadow: 0 0 0 5px rgba(169, 128, 68, .11), 0 0 18px rgba(169, 128, 68, .18);
  animation: coin-pulse 2.2s ease-in-out infinite;
}
.chart {
  width: 100%;
  height: 320px;
  border-radius: 8px;
  background:
    radial-gradient(circle at 95% 6%, rgba(185, 149, 85, .1), transparent 24%),
    linear-gradient(135deg, rgba(255, 252, 244, .82), rgba(242, 232, 212, .58));
}

@keyframes coin-pulse {
  50% {
    transform: scale(1.08);
    box-shadow: 0 0 0 6px rgba(169, 128, 68, .08), 0 0 24px rgba(169, 128, 68, .24);
  }
}
</style>
