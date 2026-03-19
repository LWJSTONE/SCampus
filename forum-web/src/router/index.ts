import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

NProgress.configure({ showSpinner: false })

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'post/:id',
        name: 'PostDetail',
        component: () => import('@/views/PostDetail.vue'),
        meta: { title: '帖子详情' }
      },
      {
        path: 'post/create',
        name: 'CreatePost',
        component: () => import('@/views/CreatePost.vue'),
        meta: { title: '发布帖子', requiresAuth: true }
      },
      {
        path: 'user/:id',
        name: 'UserProfile',
        component: () => import('@/views/UserProfile.vue'),
        meta: { title: '用户主页' }
      },
      {
        path: 'user/settings',
        name: 'UserSettings',
        component: () => import('@/views/UserSettings.vue'),
        meta: { title: '个人设置', requiresAuth: true }
      },
      {
        path: 'category/:id',
        name: 'CategoryPosts',
        component: () => import('@/views/CategoryPosts.vue'),
        meta: { title: '版块帖子' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/Search.vue'),
        meta: { title: '搜索' }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/Notifications.vue'),
        meta: { title: '消息通知', requiresAuth: true }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理首页' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'categories',
        name: 'CategoryManage',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '版块管理' }
      },
      {
        path: 'posts',
        name: 'PostManage',
        component: () => import('@/views/admin/PostManage.vue'),
        meta: { title: '帖子管理' }
      },
      {
        path: 'reports',
        name: 'ReportManage',
        component: () => import('@/views/admin/ReportManage.vue'),
        meta: { title: '举报管理' }
      },
      {
        path: 'stats',
        name: 'StatsView',
        component: () => import('@/views/admin/StatsView.vue'),
        meta: { title: '统计分析' }
      },
      {
        path: 'notices',
        name: 'NoticeManage',
        component: () => import('@/views/admin/NoticeManage.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'config',
        name: 'SystemConfig',
        component: () => import('@/views/admin/SystemConfig.vue'),
        meta: { title: '系统配置' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  NProgress.start()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - SCampus` : 'SCampus 校园论坛'

  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn

  // 需要登录但未登录
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 需要管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next({ name: 'Home' })
    return
  }

  // 已登录访问登录页
  if ((to.name === 'Login' || to.name === 'Register') && isLoggedIn) {
    next({ name: 'Home' })
    return
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
