<template>
  <div class="stats-view-page">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card" v-loading="loading">
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">用户数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" v-loading="loading">
          <div class="stat-value">{{ stats.postCount }}</div>
          <div class="stat-label">帖子数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" v-loading="loading">
          <div class="stat-value">{{ stats.commentCount }}</div>
          <div class="stat-label">评论数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" v-loading="loading">
          <div class="stat-value">{{ stats.todayActive }}</div>
          <div class="stat-label">今日活跃</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card">
      <template #header>
        <div class="chart-header">
          <span>数据趋势</span>
          <el-radio-group v-model="trendType" size="small">
            <el-radio-button value="day">日</el-radio-button>
            <el-radio-button value="week">周</el-radio-button>
            <el-radio-button value="month">月</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="chartRef" class="chart-container" v-loading="chartLoading"></div>
    </el-card>

    <el-row :gutter="16" class="detail-cards">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>用户统计</span>
          </template>
          <el-descriptions :column="2" border v-loading="userLoading">
            <el-descriptions-item label="总用户数">{{ userStats.totalUsers || '-' }}</el-descriptions-item>
            <el-descriptions-item label="今日新增">{{ userStats.todayNewUsers || '-' }}</el-descriptions-item>
            <el-descriptions-item label="活跃用户">{{ userStats.activeUsers || '-' }}</el-descriptions-item>
            <el-descriptions-item label="在线用户">{{ userStats.onlineUsers || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>互动统计</span>
          </template>
          <el-descriptions :column="2" border v-loading="interactionLoading">
            <el-descriptions-item label="总点赞数">{{ interactionStats.totalLikes || '-' }}</el-descriptions-item>
            <el-descriptions-item label="总收藏数">{{ interactionStats.totalCollects || '-' }}</el-descriptions-item>
            <el-descriptions-item label="今日点赞">{{ interactionStats.todayLikes || '-' }}</el-descriptions-item>
            <el-descriptions-item label="今日收藏">{{ interactionStats.todayCollects || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getOverviewStats, getTrendData, getUserStats, getInteractionStats } from '@/api/stats'

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const loading = ref(false)
const chartLoading = ref(false)
const userLoading = ref(false)
const interactionLoading = ref(false)

const trendType = ref<'day' | 'week' | 'month'>('day')

const stats = reactive({
  userCount: '-',
  postCount: '-',
  commentCount: '-',
  todayActive: '-'
})

const userStats = reactive({
  totalUsers: '-',
  todayNewUsers: '-',
  activeUsers: '-',
  onlineUsers: '-'
})

const interactionStats = reactive({
  totalLikes: '-',
  totalCollects: '-',
  todayLikes: '-',
  todayCollects: '-'
})

// 格式化数字
function formatNumber(num: number | undefined): string {
  if (num === undefined || num === null) return '-'
  return num.toLocaleString()
}

// 获取概览统计
async function fetchOverviewStats() {
  loading.value = true
  try {
    const data = await getOverviewStats()
    stats.userCount = formatNumber(data.userCount)
    stats.postCount = formatNumber(data.postCount)
    stats.commentCount = formatNumber(data.commentCount)
    stats.todayActive = formatNumber(data.todayActive || data.todayPostCount || 0)
  } catch (error) {
    console.error('获取概览统计失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取用户统计
async function fetchUserStats() {
  userLoading.value = true
  try {
    const data = await getUserStats()
    userStats.totalUsers = formatNumber(data.totalUsers)
    userStats.todayNewUsers = formatNumber(data.newUsersToday)
    userStats.activeUsers = formatNumber(data.activeUsers)
    userStats.onlineUsers = formatNumber(data.newUsersToday) // 使用今日新增用户作为在线用户替代
  } catch (error) {
    console.error('获取用户统计失败:', error)
  } finally {
    userLoading.value = false
  }
}

// 获取互动统计
async function fetchInteractionStats() {
  interactionLoading.value = true
  try {
    const data = await getInteractionStats()
    interactionStats.totalLikes = formatNumber(data.totalLikes)
    interactionStats.totalCollects = formatNumber(data.totalCollections)
    interactionStats.todayLikes = formatNumber(data.todayLikes)
    interactionStats.todayCollects = formatNumber(data.todayCollections)
  } catch (error) {
    console.error('获取互动统计失败:', error)
  } finally {
    interactionLoading.value = false
  }
}

// 获取趋势数据
async function fetchTrendData() {
  chartLoading.value = true
  try {
    const data = await getTrendData(trendType.value)
    updateChart(data)
  } catch (error) {
    console.error('获取趋势数据失败:', error)
    updateChart(null)
  } finally {
    chartLoading.value = false
  }
}

// 更新图表
function updateChart(data: any) {
  if (!chartRef.value) return

  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  let xData: string[] = []
  let userData: number[] = []
  let postData: number[] = []
  let commentData: number[] = []

  if (data && data.dates && data.dates.length > 0) {
    xData = data.dates
    userData = data.userData || []
    postData = data.postData || []
    commentData = data.commentData || []
  }
  // 当没有数据时，显示空图表而不是虚假数据

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '新增帖子', '新增评论'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData
    },
    yAxis: { type: 'value' },
    // 当没有数据时显示提示
    graphic: xData.length === 0 ? [{
      type: 'text',
      left: 'center',
      top: 'middle',
      style: {
        text: '暂无数据',
        fontSize: 14,
        fill: '#999'
      }
    }] : [],
    series: [
      { name: '新增用户', type: 'line', smooth: true, data: userData },
      { name: '新增帖子', type: 'line', smooth: true, data: postData },
      { name: '新增评论', type: 'line', smooth: true, data: commentData }
    ]
  }
  chart.setOption(option)
}

function handleResize() {
  chart?.resize()
}

// 监听趋势类型变化 - 添加防重复点击保护
watch(trendType, () => {
  // 如果正在加载中，不重复请求
  if (chartLoading.value) return
  fetchTrendData()
})

onMounted(() => {
  fetchOverviewStats()
  fetchUserStats()
  fetchInteractionStats()
  fetchTrendData()
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
    margin-bottom: 16px;
    
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .chart-container {
      height: 400px;
    }
  }

  .detail-cards {
    .el-card {
      margin-bottom: 16px;
    }
  }
}
</style>
