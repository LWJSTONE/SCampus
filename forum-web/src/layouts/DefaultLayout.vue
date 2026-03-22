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
          <el-button type="primary" size="large" @click="router.push('/post/create')">
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getCategoryList } from '@/api/category'
import { getUnreadCount } from '@/api/notify'
import type { CategoryVO } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const searchKeyword = ref('')
const unreadCount = ref(0)
const categories = ref<CategoryVO[]>([])

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
  if (!userStore.isLoggedIn) return
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.count || 0
  } catch (e) {
    console.error('获取未读消息数失败:', e)
  }
}

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push(`/user/${userStore.userInfo?.id}`)
      break
    case 'settings':
      router.push('/user/settings')
      break
    case 'myPosts':
      router.push(`/user/${userStore.userInfo?.id}?tab=posts`)
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        router.push('/')
        ElMessage.success('已退出登录')
      })
      break
  }
}

onMounted(() => {
  // 获取分类列表
  fetchCategories()
  // 获取未读消息数
  if (userStore.isLoggedIn) {
    fetchUnreadCount()
  }
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
