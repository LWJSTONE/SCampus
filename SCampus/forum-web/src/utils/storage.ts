/**
 * 本地存储工具函数
 * 封装 localStorage 和 sessionStorage 操作
 */

/**
 * localStorage 存储
 * @param key 键名
 * @param value 值
 */
export function setLocal(key: string, value: any): void {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (error) {
    console.error('存储数据失败：', error)
  }
}

/**
 * localStorage 读取
 * @param key 键名
 * @param defaultValue 默认值
 */
export function getLocal<T = any>(key: string, defaultValue?: T): T | null {
  try {
    const value = localStorage.getItem(key)
    if (value === null) return defaultValue ?? null
    return JSON.parse(value) as T
  } catch (error) {
    console.error('读取数据失败：', error)
    return defaultValue ?? null
  }
}

/**
 * localStorage 删除
 * @param key 键名
 */
export function removeLocal(key: string): void {
  localStorage.removeItem(key)
}

/**
 * localStorage 清空
 */
export function clearLocal(): void {
  localStorage.clear()
}

/**
 * sessionStorage 存储
 * @param key 键名
 * @param value 值
 */
export function setSession(key: string, value: any): void {
  try {
    sessionStorage.setItem(key, JSON.stringify(value))
  } catch (error) {
    console.error('存储数据失败：', error)
  }
}

/**
 * sessionStorage 读取
 * @param key 键名
 * @param defaultValue 默认值
 */
export function getSession<T = any>(key: string, defaultValue?: T): T | null {
  try {
    const value = sessionStorage.getItem(key)
    if (value === null) return defaultValue ?? null
    return JSON.parse(value) as T
  } catch (error) {
    console.error('读取数据失败：', error)
    return defaultValue ?? null
  }
}

/**
 * sessionStorage 删除
 * @param key 键名
 */
export function removeSession(key: string): void {
  sessionStorage.removeItem(key)
}

/**
 * sessionStorage 清空
 */
export function clearSession(): void {
  sessionStorage.clear()
}
