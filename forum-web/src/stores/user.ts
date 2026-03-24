import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'
import type { LoginDTO, UserInfoVO } from '@/types'

// 安全的localStorage操作辅助函数
function safeGetStorage(key: string): string {
  try {
    return localStorage.getItem(key) || ''
  } catch (e) {
    console.warn('localStorage访问失败:', e)
    return ''
  }
}

function safeSetStorage(key: string, value: string): boolean {
  try {
    localStorage.setItem(key, value)
    return true
  } catch (e) {
    console.warn('localStorage写入失败:', e)
    return false
  }
}

function safeRemoveStorage(key: string): void {
  try {
    localStorage.removeItem(key)
  } catch (e) {
    console.warn('localStorage删除失败:', e)
  }
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(safeGetStorage('token'))
  const refreshToken = ref<string>(safeGetStorage('refreshToken'))
  const userInfo = ref<UserInfoVO | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  // 修复：角色判断统一使用大写比较，与后端返回的角色格式一致
  const isAdmin = computed(() => userInfo.value?.roles?.some(r => {
    const role = typeof r === 'string' ? r.toUpperCase() : (r as any).roleCode?.toUpperCase()
    return role === 'ADMIN' || role === 'ROLE_ADMIN'
  }) || false)
  const isModerator = computed(() => userInfo.value?.roles?.some(r => {
    const role = typeof r === 'string' ? r.toUpperCase() : (r as any).roleCode?.toUpperCase()
    return role === 'MODERATOR' || role === 'ROLE_MODERATOR'
  }) || false)
  const username = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  // 登录
  async function login(loginData: LoginDTO) {
    const res = await loginApi(loginData)
    token.value = res.accessToken
    refreshToken.value = res.refreshToken
    safeSetStorage('token', res.accessToken)
    safeSetStorage('refreshToken', res.refreshToken)

    // 获取用户信息
    await fetchUserInfo()

    return res
  }

  // 登出
  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      console.error('登出失败:', e)
    } finally {
      clearAuth()
    }
  }

  // 清除认证信息
  function clearAuth() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    safeRemoveStorage('token')
    safeRemoveStorage('refreshToken')
  }

  // 获取用户信息
  async function fetchUserInfo() {
    // 如果没有token，直接返回
    if (!token.value) {
      return null
    }
    try {
      const info = await getCurrentUser()
      userInfo.value = info
      return info
    } catch (e: any) {
      console.error('获取用户信息失败:', e)
      // 只有在401错误时才清除认证信息，其他错误不清除
      // 这样可以避免网络错误导致用户被登出
      if (e?.response?.status === 401) {
        clearAuth()
      }
      return null
    }
  }

  // 检查登录状态
  async function checkLoginStatus(): Promise<boolean> {
    // 如果没有token，直接返回未登录
    if (!token.value) {
      clearAuth()
      return false
    }
    // 如果有token但没有userInfo，尝试获取用户信息
    if (!userInfo.value) {
      const info = await fetchUserInfo()
      return info !== null
    }
    return true
  }

  // 更新用户信息
  function updateUserInfo(info: Partial<UserInfoVO>) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
    }
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    isAdmin,
    isModerator,
    username,
    login,
    logout,
    clearAuth,
    fetchUserInfo,
    checkLoginStatus,
    updateUserInfo
  }
})
