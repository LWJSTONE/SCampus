import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'
import type { LoginDTO, UserInfoVO } from '@/types'

// 角色类型定义
interface RoleObject {
  roleCode?: string
  roleName?: string
  id?: number
}

type RoleType = string | RoleObject

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

/**
 * 从角色对象中提取角色代码
 * 支持字符串格式和对象格式的角色
 */
function getRoleCode(role: RoleType): string | null {
  if (typeof role === 'string') {
    return role.toUpperCase()
  }
  if (role && typeof role === 'object') {
    const roleCode = (role as RoleObject).roleCode
    return roleCode ? roleCode.toUpperCase() : null
  }
  return null
}

/**
 * 检查是否拥有指定角色
 */
function hasRole(roles: RoleType[] | undefined, targetRoles: string[]): boolean {
  if (!roles || !Array.isArray(roles)) {
    return false
  }
  return roles.some(r => {
    const roleCode = getRoleCode(r)
    return roleCode !== null && (targetRoles.includes(roleCode) || targetRoles.includes(`ROLE_${roleCode}`))
  })
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(safeGetStorage('token'))
  const refreshToken = ref<string>(safeGetStorage('refreshToken'))
  const userInfo = ref<UserInfoVO | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  // 修复：角色判断统一使用大写比较，与后端返回的角色格式一致
  // 使用类型守卫确保角色类型安全
  const isAdmin = computed(() => hasRole(userInfo.value?.roles, ['ADMIN']))
  const isModerator = computed(() => hasRole(userInfo.value?.roles, ['MODERATOR']))
  const username = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  // 登录
  async function login(loginData: LoginDTO) {
    const res = await loginApi(loginData)
    
    // 验证返回的token是否有效
    if (!res.accessToken) {
      throw new Error('登录失败：未获取到访问令牌')
    }
    
    token.value = res.accessToken
    refreshToken.value = res.refreshToken || ''
    safeSetStorage('token', res.accessToken)
    if (res.refreshToken) {
      safeSetStorage('refreshToken', res.refreshToken)
    }

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
      
      // 验证返回的用户信息
      if (!info || !info.id) {
        console.warn('获取用户信息返回无效数据')
        return null
      }
      
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
    // 验证token是否仍然有效（可选：可以添加token过期检查）
    return true
  }

  // 更新用户信息
  function updateUserInfo(info: Partial<UserInfoVO>) {
    if (userInfo.value) {
      // 深度合并，确保嵌套对象也被正确处理
      userInfo.value = { 
        ...userInfo.value, 
        ...info,
        // 保留roles和permissions的原始引用（如果没有更新）
        roles: info.roles || userInfo.value.roles,
        permissions: info.permissions || userInfo.value.permissions
      }
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
