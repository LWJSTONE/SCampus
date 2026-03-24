<template>
  <el-container class="layout-container">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-left">
        <router-link to="/" class="logo">
          <el-icon size="24"><School /></el-icon>
          <span class="logo-text">SCampus</span>
        </router-link>
      </div>

      <div class="header-center">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索帖子..."
          prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
          style="width: 400px"
        />
      </div>

      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-button text @click="router.push('/notifications')">
              <el-icon size="20"><Bell /></el-icon>
            </el-button>
          </el-badge>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :src="userStore.userInfo?.avatar" :size="32">
                {{ userStore.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人主页
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>个人设置
                </el-dropdown-item>
                <el-dropdown-item command="myPosts">
                  <el-icon><Document /></el-icon>我的帖子
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided>
                  <el-icon><Setting /></el-icon>管理后台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
          <el-button @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 左侧边栏 -->
      <el-aside width="220px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-divider content-position="left">版块</el-divider>

          <el-menu-item
            v-for="category in categories"
            :key="category.id"
            :index="`/category/${category.id}`"
          >
            <el-icon><Folder /></el-icon>
            <span>{{ category.name }}</span>
          </el-menu-item>
        </el-menu>

        <!-- 发帖按钮 -->
        <div class="post-button">
          <el-button type="primary" size="large" @click="handleCreatePost">
            <el-icon><Edit /></el-icon>
            发布帖子
          </el-button>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 页脚 -->
    <el-footer class="footer">
      <p>© 2024 SCampus 校园论坛系统 | 基于Spring Cloud Alibaba构建</p>
    </el-footer>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getCategoryList } from '@/api/category'
import { getUnreadCount } from '@/api/notify'
import type { CategoryVO } from '@/types'
import DOMPurify from 'dompurify'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const searchKeyword = ref('')
const unreadCount = ref(0)
const categories = ref<CategoryVO[]>([])
let unreadTimer: ReturnType<typeof setInterval> | null = null

const activeMenu = computed(() => route.path)

// 获取分类列表
async function fetchCategories() {
  try {
    const res = await getCategoryList()
    categories.value = res || []
  } catch (e) {
    console.error('获取分类失败:', e)
    // 如果获取失败，使用空数组
    categories.value = []
  }
}

// 获取未读消息数
async function fetchUnreadCount() {
  if (!userStore.isLoggedIn) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.count || 0
  } catch (e) {
    console.error('获取未读消息数失败:', e)
  }
}

function handleSearch() {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    // 使用DOMPurify进行完善的XSS防护
    // 对于搜索关键词，只需要保留纯文本，移除所有HTML标签和危险字符
    const sanitizedKeyword = DOMPurify.sanitize(keyword, {
      ALLOWED_TAGS: [], // 不允许任何HTML标签
      ALLOWED_ATTR: [], // 不允许任何属性
      KEEP_CONTENT: true // 保留文本内容
    }).trim()
    
    if (!sanitizedKeyword) {
      // 如果过滤后为空，提示用户
      ElMessage.warning('请输入有效的搜索关键词')
      return
    }
    
    if (sanitizedKeyword !== keyword) {
      // 如果检测到XSS字符，使用净化后的关键词
      searchKeyword.value = sanitizedKeyword
    }
    
    // 使用 q 参数名，与 Search.vue 中 route.query.q 保持一致
    router.push({ path: '/search', query: { q: sanitizedKeyword } })
  }
}

function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push(`/user/${userStore.userInfo?.id}`).catch(() => {})
      break
    case 'settings':
      router.push('/user/settings').catch(() => {})
      break
    case 'myPosts':
      router.push(`/user/${userStore.userInfo?.id}?tab=posts`).catch(() => {})
      break
    case 'admin':
      router.push('/admin').catch(() => {})
      break
    case 'logout':
      handleLogout()
      break
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.logout()
    router.push('/').catch(() => {})
    ElMessage.success('已退出登录')
  } catch {
    // 用户取消操作，不需要处理
  }
}

// 监听登录状态变化
watch(
  () => userStore.isLoggedIn,
  (isLoggedIn) => {
    if (isLoggedIn) {
      fetchUnreadCount()
      startUnreadPolling()
    } else {
      unreadCount.value = 0
      stopUnreadPolling()
    }
  }
)

// 页面可见性变化处理
function handleVisibilityChange() {
  if (document.hidden) {
    // 页面不可见时，暂停轮询以节省资源
    stopUnreadPolling()
  } else {
    // 页面可见时，立即刷新一次并恢复轮询
    fetchUnreadCount()
    if (userStore.isLoggedIn) {
      startUnreadPolling()
    }
  }
}

// 定时刷新未读消息数
function startUnreadPolling() {
  if (unreadTimer) return
  unreadTimer = setInterval(() => {
    // 只有在页面可见时才进行请求
    if (!document.hidden) {
      fetchUnreadCount()
    }
  }, 60000) // 每分钟刷新一次
}

function stopUnreadPolling() {
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
}

// 发帖按钮点击处理
function handleCreatePost() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/post/create')
}

onMounted(() => {
  // 获取分类列表
  fetchCategories()
  // 获取未读消息数
  if (userStore.isLoggedIn) {
    fetchUnreadCount()
    startUnreadPolling()
  }
  // 监听页面可见性变化
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  stopUnreadPolling()
  // 移除页面可见性监听
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped lang="scss">
.layout-container {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;

  .header-left {
    display: flex;
    align-items: center;

    .logo {
      display: flex;
      align-items: center;
      text-decoration: none;
      color: var(--el-color-primary);
      font-size: 20px;
      font-weight: bold;

      .logo-text {
        margin-left: 8px;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.main-container {
  height: calc(100vh - 60px - 40px);
}

.sidebar {
  background: #fff;
  border-right: 1px solid #e4e7ed;

  .sidebar-menu {
    border-right: none;
  }

  .post-button {
    padding: 20px;
    text-align: center;
  }
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
}

.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  font-size: 12px;
  color: #909399;
}

.notification-badge {
  margin-right: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
