<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in statistics" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
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
                <el-radio-button label="day">日</el-radio-button>
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
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
            <span>最新帖子</span>
          </template>
          <el-table :data="latestPosts" style="width: 100%">
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column prop="username" label="作者" width="100" />
            <el-table-column prop="createTime" label="时间" width="120" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待处理事项</span>
          </template>
          <div class="todo-list">
            <div class="todo-item" v-for="todo in todoList" :key="todo.title">
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
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: ECharts
let pieChart: ECharts

const trendType = ref('day')

const statistics = ref([
  { title: '用户总数', value: '12,345', icon: 'User', color: '#409EFF', trend: 12.5, type: 'primary' },
  { title: '帖子总数', value: '8,888', icon: 'Document', color: '#67C23A', trend: 8.2, type: 'success' },
  { title: '评论总数', value: '45,678', icon: 'ChatDotRound', color: '#E6A23C', trend: -3.1, type: 'warning' },
  { title: '今日活跃', value: '1,234', icon: 'TrendCharts', color: '#F56C6C', trend: 15.8, type: 'danger' }
])

const latestPosts = ref([
  { title: '这是一个示例帖子标题', username: '张三', createTime: '10分钟前' },
  { title: '校园生活分享', username: '李四', createTime: '30分钟前' },
  { title: '技术交流讨论', username: '王五', createTime: '1小时前' },
  { title: '失物招领信息', username: '赵六', createTime: '2小时前' },
  { title: '二手交易转让', username: '钱七', createTime: '3小时前' }
])

const todoList = ref([
  { title: '待审核帖子', count: 5, icon: 'Document', color: '#E6A23C', type: 'warning' },
  { title: '待处理举报', count: 3, icon: 'Warning', color: '#F56C6C', type: 'danger' },
  { title: '待回复反馈', count: 8, icon: 'ChatDotRound', color: '#409EFF', type: 'primary' },
  { title: '系统公告待发布', count: 1, icon: 'Bell', color: '#67C23A', type: 'success' }
])

function initTrendChart() {
  if (!trendChartRef.value) return

  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['新用户', '新帖子', '新评论'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [
      { name: '新用户', type: 'line', smooth: true, data: [120, 132, 101, 134, 90, 230, 210] },
      { name: '新帖子', type: 'line', smooth: true, data: [220, 182, 191, 234, 290, 330, 310] },
      { name: '新评论', type: 'line', smooth: true, data: [150, 232, 201, 154, 190, 330, 410] }
    ]
  }
  trendChart.setOption(option)
}

function initPieChart() {
  if (!pieChartRef.value) return

  pieChart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '帖子分布',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 1048, name: '校园生活' },
          { value: 735, name: '学术讨论' },
          { value: 580, name: '技术交流' },
          { value: 484, name: '二手交易' },
          { value: 300, name: '其他' }
        ]
      }
    ]
  }
  pieChart.setOption(option)
}

onMounted(() => {
  initTrendChart()
  initPieChart()
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

watch(trendType, () => {
  // 重新加载数据
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
    .todo-list {
      .todo-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

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
