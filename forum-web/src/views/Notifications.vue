<template>
  <div class="notifications-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>消息通知</span>
          <el-button link type="primary" @click="markAllRead">全部已读</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" v-loading="loading">
        <el-tab-pane label="全部" name="all">
          <NotificationList :notifications="notifications" @read="markRead" />
        </el-tab-pane>
        <el-tab-pane label="评论" name="comment">
          <NotificationList :notifications="commentNotifications" @read="markRead" />
        </el-tab-pane>
        <el-tab-pane label="点赞" name="like">
          <NotificationList :notifications="likeNotifications" @read="markRead" />
        </el-tab-pane>
        <el-tab-pane label="系统" name="system">
          <NotificationList :notifications="systemNotifications" @read="markRead" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h, defineComponent } from 'vue'
import { ElMessage } from 'element-plus'
import type { NoticeVO } from '@/types'
import { getNoticeList, markAsRead, markAllAsRead } from '@/api/notify'

// 简单的通知列表组件
const NotificationList = defineComponent({
  props: {
    notifications: { type: Array as () => NoticeVO[], default: () => [] }
  },
  emits: ['read'],
  setup(props, { emit }) {
    return () => {
      if (props.notifications.length === 0) {
        return h('div', { class: 'empty' }, '暂无通知')
      }
      return h('div', { class: 'notification-list' },
        props.notifications.map(item =>
          h('div', {
            class: ['notification-item', item.isRead ? 'read' : 'unread'],
            onClick: () => emit('read', item.id)
          }, [
            h('div', { class: 'title' }, item.title),
            h('div', { class: 'content' }, item.content),
            h('div', { class: 'time' }, item.createTime)
          ])
        )
      )
    }
  }
})

const activeTab = ref('all')
const notifications = ref<NoticeVO[]>([])
const loading = ref(false)

const commentNotifications = computed(() => 
  notifications.value.filter(n => n.type === 1)
)
const likeNotifications = computed(() => 
  notifications.value.filter(n => n.type === 2)
)
const systemNotifications = computed(() => 
  notifications.value.filter(n => n.type === 0)
)

async function fetchNotifications() {
  loading.value = true
  try {
    const res = await getNoticeList({ current: 1, size: 100 })
    notifications.value = res.records || res.list || []
  } catch (e: any) {
    console.error('获取通知失败:', e)
    ElMessage.error(e?.message || '获取通知失败')
    // 如果API调用失败，显示空列表而不是模拟数据
    notifications.value = []
  } finally {
    loading.value = false
  }
}

async function markRead(id: number) {
  const notification = notifications.value.find(n => n.id === id)
  if (notification) {
    try {
      await markAsRead(id)
      notification.isRead = true
    } catch (e) {
      console.error('标记已读失败:', e)
      ElMessage.error('操作失败')
    }
  }
}

async function markAllRead() {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    console.error('全部标记已读失败:', e)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped lang="scss">
.notifications-page {
  max-width: 900px;
  margin: 0 auto;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  :deep(.notification-list) {
    .notification-item {
      padding: 16px;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;

      &.unread {
        background-color: #f5f7fa;
      }

      &:hover {
        background-color: #f0f2f5;
      }

      .title {
        font-weight: 500;
        margin-bottom: 4px;
      }

      .content {
        color: #606266;
        font-size: 14px;
        margin-bottom: 4px;
      }

      .time {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  :deep(.empty) {
    text-align: center;
    padding: 40px;
    color: #909399;
  }
}
</style>
