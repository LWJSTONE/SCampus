<template>
  <div class="stats-view-page">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">用户数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.postCount }}</div>
          <div class="stat-label">帖子数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.commentCount }}</div>
          <div class="stat-label">评论数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.todayActive }}</div>
          <div class="stat-label">今日活跃</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card">
      <template #header>
        <span>数据趋势</span>
      </template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const stats = reactive({
  userCount: 1234,
  postCount: 5678,
  commentCount: 9012,
  todayActive: 345
})

function initChart() {
  if (!chartRef.value) return
  
  chart = echarts.init(chartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '新增帖子', '新增评论'] },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [
      { name: '新增用户', type: 'line', data: [120, 132, 101, 134, 90, 230, 210] },
      { name: '新增帖子', type: 'line', data: [220, 182, 191, 234, 290, 330, 310] },
      { name: '新增评论', type: 'line', data: [150, 232, 201, 154, 190, 330, 410] }
    ]
  }
  chart.setOption(option)
}

function handleResize() {
  chart?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  chart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.stats-view-page {
  .stat-cards {
    margin-bottom: 16px;

    .stat-card {
      text-align: center;
      padding: 16px 0;

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: var(--el-color-primary);
      }

      .stat-label {
        margin-top: 8px;
        color: #909399;
      }
    }
  }

  .chart-card {
    .chart-container {
      height: 400px;
    }
  }
}
</style>
