import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'
import type { LoginDTO, UserInfoVO } from '@/types'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const refreshToken = ref<string>(localStorage.getItem('refreshToken') || '')
  const userInfo = ref<UserInfoVO | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.roles?.some(r => r === 'admin') || false)
  const isModerator = computed(() => userInfo.value?.roles?.some(r => r === 'moderator') || false)
  const username = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  // 登录
  async function login(loginData: LoginDTO) {
    const res = await loginApi(loginData)
    token.value = res.accessToken
    refreshToken.value = res.refreshToken
    localStorage.setItem('token', res.accessToken)
    localStorage.setItem('refreshToken', res.refreshToken)

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
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  // 获取用户信息
  async function fetchUserInfo() {
    try {
      const info = await getCurrentUser()
      userInfo.value = info
    } catch (e) {
      console.error('获取用户信息失败:', e)
      clearAuth()
    }
  }

  // 检查登录状态
  async function checkLoginStatus() {
    if (token.value && !userInfo.value) {
      await fetchUserInfo()
    }
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
