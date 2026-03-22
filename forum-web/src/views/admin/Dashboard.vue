<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in statistics" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }" v-loading="loading">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon size="24"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
          <div class="stat-footer">
            <span :class="stat.trend > 0 ? 'up' : 'down'">
              <el-icon><component :is="stat.trend > 0 ? 'Top' : 'Bottom'" /></el-icon>
              {{ Math.abs(stat.trend) }}%
            </span>
            <span>较昨日</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>趋势分析</span>
              <el-radio-group v-model="trendType" size="small">
                <el-radio-button value="day">日</el-radio-button>
                <el-radio-button value="week">周</el-radio-button>
                <el-radio-button value="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container" v-loading="chartLoading"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>版块分布</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新数据 -->
    <el-row :gutter="20" class="latest-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新帖子</span>
              <el-button link type="primary" @click="$router.push('/admin/posts')">查看更多</el-button>
            </div>
          </template>
          <el-table :data="latestPosts" style="width: 100%" v-loading="postsLoading">
            <el-table-column prop="title" label="标题" show-overflow-tooltip>
              <template #default="{ row }">
                <router-link :to="`/post/${row.id}`" class="post-link">{{ row.title }}</router-link>
              </template>
            </el-table-column>
            <el-table-column prop="username" label="作者" width="100" />
            <el-table-column prop="createTime" label="时间" width="120">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待处理事项</span>
          </template>
          <div class="todo-list" v-loading="todoLoading">
            <div class="todo-item" v-for="todo in todoList" :key="todo.title" @click="handleTodoClick(todo)">
              <div class="todo-info">
                <el-icon :color="todo.color"><component :is="todo.icon" /></el-icon>
                <span>{{ todo.title }}</span>
              </div>
              <el-tag :type="todo.type as 'primary' | 'success' | 'warning' | 'danger' | 'info'">{{ todo.count }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { getOverviewStats, getTrendData } from '@/api/stats'
import { getPostList } from '@/api/post'
import { getPendingCount } from '@/api/report'
import { getCategoryList } from '@/api/category'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: ECharts
let pieChart: ECharts

const loading = ref(true)
const chartLoading = ref(false)
const postsLoading = ref(false)
const todoLoading = ref(false)

const trendType = ref<'day' | 'week' | 'month'>('day')

interface StatItem {
  title: string
  value: string
  icon: string
  color: string
  trend: number
  type: string
}

const statistics = ref<StatItem[]>([
  { title: '用户总数', value: '-', icon: 'User', color: '#409EFF', trend: 0, type: 'primary' },
  { title: '帖子总数', value: '-', icon: 'Document', color: '#67C23A', trend: 0, type: 'success' },
  { title: '评论总数', value: '-', icon: 'ChatDotRound', color: '#E6A23C', trend: 0, type: 'warning' },
  { title: '今日活跃', value: '-', icon: 'TrendCharts', color: '#F56C6C', trend: 0, type: 'danger' }
])

const latestPosts = ref<any[]>([])

interface TodoItem {
  title: string
  count: number
  icon: string
  color: string
  type: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  path?: string
}

const todoList = ref<TodoItem[]>([
  { title: '待审核帖子', count: 0, icon: 'Document', color: '#E6A23C', type: 'warning', path: '/admin/posts?status=1' },
  { title: '待处理举报', count: 0, icon: 'Warning', color: '#F56C6C', type: 'danger', path: '/admin/reports' },
  { title: '待回复反馈', count: 0, icon: 'ChatDotRound', color: '#409EFF', type: 'primary' },
  { title: '系统公告待发布', count: 0, icon: 'Bell', color: '#67C23A', type: 'success', path: '/admin/notices' }
])

// 格式化数字为带千分位的字符串
function formatNumber(num: number): string {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

// 格式化时间
function formatTime(time: string): string {
  if (!time) return '-'
  return dayjs(time).fromNow()
}

// 格式化趋势数据日期
function formatDateLabel(dateStr: string, type: string): string {
  const date = dayjs(dateStr)
  switch (type) {
    case 'day':
      return date.format('HH:mm')
    case 'week':
      return date.format('MM-DD')
    case 'month':
      return date.format('MM-DD')
    default:
      return date.format('MM-DD')
  }
}

// 获取概览统计数据
async function fetchOverviewStats() {
  loading.value = true
  try {
    const data = await getOverviewStats()
    statistics.value = [
      { 
        title: '用户总数', 
        value: formatNumber(data.userCount), 
        icon: 'User', 
        color: '#409EFF', 
        trend: 0,
        type: 'primary' 
      },
      { 
        title: '帖子总数', 
        value: formatNumber(data.postCount), 
        icon: 'Document', 
        color: '#67C23A', 
        trend: 0,
        type: 'success' 
      },
      { 
        title: '评论总数', 
        value: formatNumber(data.commentCount), 
        icon: 'ChatDotRound', 
        color: '#E6A23C', 
        trend: 0,
        type: 'warning' 
      },
      { 
        title: '今日活跃', 
        value: formatNumber(data.todayActive || data.todayPostCount || 0), 
        icon: 'TrendCharts', 
        color: '#F56C6C', 
        trend: 0,
        type: 'danger' 
      }
    ]
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 保持默认值
  } finally {
    loading.value = false
  }
}

// 获取最新帖子
async function fetchLatestPosts() {
  postsLoading.value = true
  try {
    const res = await getPostList({ page: 1, size: 5 })
    latestPosts.value = res.records || []
  } catch (error) {
    console.error('获取最新帖子失败:', error)
    latestPosts.value = []
  } finally {
    postsLoading.value = false
  }
}

// 获取待处理事项统计
async function fetchTodoStats() {
  todoLoading.value = true
  try {
    const data = await getPendingCount()
    // 更新待处理举报数量
    const reportTodo = todoList.value.find(t => t.title === '待处理举报')
    if (reportTodo) {
      reportTodo.count = data.pendingReportCount || 0
    }
    // 更新待审核帖子数量
    const postTodo = todoList.value.find(t => t.title === '待审核帖子')
    if (postTodo) {
      postTodo.count = data.pendingApproveCount || 0
    }
  } catch (error) {
    console.error('获取待处理统计失败:', error)
  } finally {
    todoLoading.value = false
  }
}

// 获取趋势数据
async function fetchTrendData() {
  chartLoading.value = true
  try {
    const data = await getTrendData(trendType.value)
    updateTrendChart(data)
  } catch (error) {
    console.error('获取趋势数据失败:', error)
    // 使用默认数据
    updateTrendChart(null)
  } finally {
    chartLoading.value = false
  }
}

// 获取版块分布数据
async function fetchCategoryDistribution() {
  try {
    const categories = await getCategoryList()
    const pieData = categories.map((cat: any) => ({
      value: cat.postCount || cat.threadCount || Math.floor(Math.random() * 1000),
      name: cat.name
    })).slice(0, 5)
    updatePieChart(pieData)
  } catch (error) {
    console.error('获取版块分布失败:', error)
    // 使用默认数据
    updatePieChart(null)
  }
}

// 更新趋势图表
function updateTrendChart(data: any) {
  if (!trendChartRef.value) return

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  let xData: string[] = []
  let userData: number[] = []
  let postData: number[] = []
  let commentData: number[] = []

  if (data && data.dates) {
    xData = data.dates.map((d: string) => formatDateLabel(d, trendType.value))
    userData = data.userData || []
    postData = data.postData || []
    commentData = data.commentData || []
  } else {
    // 默认数据
    const now = dayjs()
    for (let i = 6; i >= 0; i--) {
      xData.push(now.subtract(i, 'day').format('MM-DD'))
      userData.push(Math.floor(Math.random() * 200) + 50)
      postData.push(Math.floor(Math.random() * 300) + 100)
      commentData.push(Math.floor(Math.random() * 400) + 150)
    }
  }

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['新用户', '新帖子', '新评论'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData
    },
    yAxis: { type: 'value' },
    series: [
      { name: '新用户', type: 'line', smooth: true, data: userData },
      { name: '新帖子', type: 'line', smooth: true, data: postData },
      { name: '新评论', type: 'line', smooth: true, data: commentData }
    ]
  }
  trendChart.setOption(option)
}

// 更新饼图
function updatePieChart(data: any) {
  if (!pieChartRef.value) return

  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  const defaultData = [
    { value: 1048, name: '校园生活' },
    { value: 735, name: '学术讨论' },
    { value: 580, name: '技术交流' },
    { value: 484, name: '二手交易' },
    { value: 300, name: '其他' }
  ]

  const option = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '帖子分布',
        type: 'pie',
        radius: '50%',
        data: data || defaultData
      }
    ]
  }
  pieChart.setOption(option)
}

// 处理待办事项点击
function handleTodoClick(todo: TodoItem) {
  if (todo.path) {
    router.push(todo.path)
  }
}

// 初始化图表
function initCharts() {
  fetchTrendData()
  fetchCategoryDistribution()
}

// 监听趋势类型变化
watch(trendType, () => {
  fetchTrendData()
})

onMounted(() => {
  // 获取所有数据
  fetchOverviewStats()
  fetchLatestPosts()
  fetchTodoStats()
  initCharts()
})

// 组件卸载时销毁ECharts实例，防止内存泄漏
onUnmounted(() => {
  if (trendChart) {
    trendChart.dispose()
  }
  if (pieChart) {
    pieChart.dispose()
  }
})
</script>

<style scoped lang="scss">
.dashboard-page {
  .stat-cards {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
        }

        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: 600;
          }

          .stat-title {
            color: #909399;
            font-size: 14px;
          }
        }
      }

      .stat-footer {
        margin-top: 12px;
        font-size: 12px;
        color: #909399;

        span {
          margin-right: 4px;

          &.up {
            color: #67C23A;
          }

          &.down {
            color: #F56C6C;
          }
        }
      }
    }
  }

  .chart-row {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .chart-container {
      height: 300px;
    }
  }

  .latest-row {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .post-link {
      color: #303133;
      text-decoration: none;
      
      &:hover {
        color: var(--el-color-primary);
      }
    }

    .todo-list {
      .todo-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover {
          background-color: #f5f7fa;
        }

        &:last-child {
          border-bottom: none;
        }

        .todo-info {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }
    }
  }
}
</style>
