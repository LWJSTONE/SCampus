<template>
  <!-- 权限检查：使用v-if控制渲染，避免页面闪烁 -->
  <div v-if="!authChecked" class="auth-loading">
    <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    <span>正在验证权限...</span>
  </div>
  <el-container v-else-if="hasPermission" class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <el-icon size="24"><Setting /></el-icon>
        <span v-show="!isCollapse">管理后台</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon>
          <span>控制台</span>
        </el-menu-item>

        <el-sub-menu index="user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/users">用户列表</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="content">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/admin/categories">版块管理</el-menu-item>
          <el-menu-item index="/admin/posts">帖子管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/admin/reports">
          <el-icon><Warning /></el-icon>
          <span>举报管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/notices">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/stats">
          <el-icon><TrendCharts /></el-icon>
          <span>统计分析</span>
        </el-menu-item>

        <el-menu-item index="/admin/config">
          <el-icon><Tools /></el-icon>
          <span>系统配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-button text @click="isCollapse = !isCollapse">
            <el-icon size="20">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-button text @click="router.push('/').catch(() => {})">
            <el-icon><HomeFilled /></el-icon>
            前台首页
          </el-button>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :src="userStore.userInfo?.avatar" :size="32">
                {{ userStore.username?.charAt(0) }}
              </el-avatar>
              <span>{{ userStore.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
  <!-- 无权限提示 -->
  <div v-else class="no-permission">
    <el-icon :size="48"><WarningFilled /></el-icon>
    <p>您没有访问管理后台的权限</p>
    <el-button type="primary" @click="router.push('/').catch(() => {})">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, WarningFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const currentTitle = computed(() => {
  const meta = route.meta
  return (meta?.title as string) || ''
})

function handleCommand(command: string) {
  if (command === 'logout') {
    handleLogout()
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
    // 统一退出后跳转到首页，与 DefaultLayout 保持一致
    router.push('/').catch(() => {})
    ElMessage.success('已退出登录')
  } catch {
    // 用户取消操作，不需要处理
  }
}

// ============ 权限检查逻辑优化 ============
// 使用响应式变量控制渲染，避免页面闪烁
const authChecked = ref(false)
const hasPermission = ref(false)

/**
 * 检查管理员权限
 * 修复：在setup阶段立即检查权限，使用v-if控制渲染
 * 避免onMounted中检查导致的页面闪烁问题
 */
async function checkPermission() {
  // 如果没有登录，跳转登录页
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login').catch(() => {})
    return
  }

  // 如果还没有用户信息，尝试获取
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      ElMessage.error('获取用户信息失败')
      router.push('/login').catch(() => {})
      return
    }
  }

  // 检查是否是管理员
  if (!userStore.isAdmin) {
    hasPermission.value = false
  } else {
    hasPermission.value = true
  }

  authChecked.value = true
}

// 组件创建时立即检查权限（而不是onMounted）
checkPermission()
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;

  .sidebar {
    background: #304156;
    transition: width 0.3s;

    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #fff;
      font-size: 18px;
      font-weight: bold;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }

    .el-menu {
      border-right: none;
    }
  }

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;
    padding: 0 20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
      }
    }
  }

  .main-content {
    background: #f0f2f5;
    padding: 20px;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 权限加载中状态
.auth-loading {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #409EFF;
  font-size: 14px;
}

// 无权限提示
.no-permission {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #909399;

  p {
    font-size: 16px;
    margin: 0;
  }
}
</style>
