<template>
  <div class="notifications-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>消息通知</span>
          <el-button link type="primary" @click="markAllRead">全部已读</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
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
  // 模拟数据
  notifications.value = [
    { id: 1, title: '系统公告', content: '欢迎使用SCampus校园论坛系统', type: 0, isRead: false, createTime: '2024-01-01 10:00:00' },
    { id: 2, title: '评论回复', content: '用户A回复了你的帖子', type: 1, isRead: false, createTime: '2024-01-02 11:00:00' }
  ]
}

async function markRead(id: number) {
  const notification = notifications.value.find(n => n.id === id)
  if (notification) {
    notification.isRead = true
  }
}

async function markAllRead() {
  notifications.value.forEach(n => n.isRead = true)
  ElMessage.success('已全部标记为已读')
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
