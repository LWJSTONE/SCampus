<template>
  <div class="notifications-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>消息通知</span>
          <div class="header-actions">
            <el-button link type="primary" @click="refresh" :loading="loading">刷新</el-button>
            <el-button link type="primary" @click="markAllRead" :loading="markAllLoading">全部已读</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" v-loading="loading">
        <el-tab-pane label="全部" name="all">
          <NotificationList :notifications="notifications" @read="markRead" />
          <div class="load-more" v-if="hasMore">
            <el-button @click="loadMore" :loading="loading" :disabled="loading">加载更多</el-button>
          </div>
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
import { ref, computed, onMounted, h, defineComponent, type PropType } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { NoticeVO } from '@/types'
import { getNoticeList, markAsRead, markAllAsRead } from '@/api/notify'
import { useUserStore } from '@/stores/user'

// 简单的通知列表组件（带TypeScript类型支持）
const NotificationList = defineComponent({
  name: 'NotificationList',
  props: {
    notifications: { 
      type: Array as PropType<NoticeVO[]>, 
      default: () => [] 
    }
  },
  emits: ['read'],
  setup(props, { emit }) {
    return () => {
      if (props.notifications.length === 0) {
        return h('div', { class: 'empty' }, '暂无通知')
      }
      return h('div', { class: 'notification-list' },
        props.notifications.map((item: NoticeVO) =>
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

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('all')
const notifications = ref<NoticeVO[]>([])
const loading = ref(false)
const page = ref(1)
const size = 20
const hasMore = ref(true)
const markAllLoading = ref(false)

// 加载更多状态
const loadMoreTriggered = ref(false)

const commentNotifications = computed(() => 
  notifications.value.filter(n => n.type === 1)
)
const likeNotifications = computed(() => 
  notifications.value.filter(n => n.type === 2)
)
const systemNotifications = computed(() => 
  notifications.value.filter(n => n.type === 0)
)

async function fetchNotifications(append = false) {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login').catch(() => {})
    return
  }
  
  loading.value = true
  try {
    const res = await getNoticeList({ current: page.value, size })
    const records = res.records || res.list || []
    
    if (append) {
      notifications.value = [...notifications.value, ...records]
    } else {
      notifications.value = records
    }
    
    // 判断是否还有更多数据
    const total = res.total || 0
    hasMore.value = notifications.value.length < total
  } catch (e: any) {
    console.error('获取通知失败:', e)
    ElMessage.error(e?.message || '获取通知失败')
    // 如果API调用失败，显示空列表而不是模拟数据
    if (!append) {
      notifications.value = []
    }
  } finally {
    loading.value = false
    loadMoreTriggered.value = false
  }
}

async function markRead(id: number) {
  const notification = notifications.value.find(n => n.id === id)
  if (notification) {
    // 保存原始状态用于回滚
    const originalReadState = notification.isRead
    
    try {
      // 乐观更新 UI
      notification.isRead = true
      await markAsRead(id)
    } catch (e) {
      console.error('标记已读失败:', e)
      // 回滚状态
      notification.isRead = originalReadState
      ElMessage.error('操作失败')
    }
  }
}

async function markAllRead() {
  // 检查是否有未读通知
  const unreadNotifications = notifications.value.filter(n => !n.isRead)
  if (unreadNotifications.length === 0) {
    ElMessage.info('没有未读通知')
    return
  }
  
  // 防止重复点击
  if (markAllLoading.value) return
  
  markAllLoading.value = true
  
  // 保存原始状态用于回滚
  const originalStates = unreadNotifications.map(n => ({ id: n.id, isRead: n.isRead }))
  
  try {
    // 乐观更新 UI
    unreadNotifications.forEach(n => n.isRead = true)
    await markAllAsRead()
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    console.error('全部标记已读失败:', e)
    // 回滚状态
    originalStates.forEach(({ id, isRead }) => {
      const notification = notifications.value.find(n => n.id === id)
      if (notification) {
        notification.isRead = isRead
      }
    })
    ElMessage.error('操作失败')
  } finally {
    markAllLoading.value = false
  }
}

// 加载更多
async function loadMore() {
  // 防止重复触发加载
  if (loadMoreTriggered.value || loading.value || !hasMore.value) return
  
  loadMoreTriggered.value = true
  page.value++
  await fetchNotifications(true)
}

// 刷新通知列表
async function refresh() {
  // 防止重复刷新
  if (loading.value) return
  
  page.value = 1
  hasMore.value = true
  await fetchNotifications(false)
}

onMounted(() => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login').catch(() => {})
    return
  }
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
