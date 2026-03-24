/**
 * 认证工具函数
 * 处理 Token 的存储和获取
 */

// 与 stores/user.ts 保持一致的 token key
const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'

/**
 * 获取 Token
 */
export function getToken(): string {
  try {
    return localStorage.getItem(TOKEN_KEY) || ''
  } catch (error) {
    console.warn('读取Token失败（可能是隐私模式）：', error)
    return ''
  }
}

/**
 * 设置 Token
 * @param token Token 值
 * @returns 是否设置成功
 */
export function setToken(token: string): boolean {
  try {
    localStorage.setItem(TOKEN_KEY, token)
    return true
  } catch (error) {
    console.warn('设置Token失败（可能是隐私模式）：', error)
    return false
  }
}

/**
 * 移除 Token
 */
export function removeToken(): void {
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch (error) {
    console.warn('移除Token失败：', error)
  }
}

/**
 * 获取刷新 Token
 */
export function getRefreshToken(): string {
  try {
    return localStorage.getItem(REFRESH_TOKEN_KEY) || ''
  } catch (error) {
    console.warn('读取刷新Token失败（可能是隐私模式）：', error)
    return ''
  }
}

/**
 * 设置刷新 Token
 * @param token Token 值
 * @returns 是否设置成功
 */
export function setRefreshToken(token: string): boolean {
  try {
    localStorage.setItem(REFRESH_TOKEN_KEY, token)
    return true
  } catch (error) {
    console.warn('设置刷新Token失败（可能是隐私模式）：', error)
    return false
  }
}

/**
 * 移除刷新 Token
 */
export function removeRefreshToken(): void {
  try {
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  } catch (error) {
    console.warn('移除刷新Token失败：', error)
  }
}

/**
 * 检查是否已登录
 */
export function isLoggedIn(): boolean {
  return !!getToken()
}

/**
 * 清除所有认证信息
 */
export function clearAuth(): void {
  try {
    removeToken()
    removeRefreshToken()
  } catch (error) {
    console.warn('清除认证信息失败：', error)
  }
}
