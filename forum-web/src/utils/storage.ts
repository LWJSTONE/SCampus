/**
 * 本地存储工具函数
 * 封装 localStorage 和 sessionStorage 操作
 */

/**
 * localStorage 存储
 * @param key 键名
 * @param value 值
 * @returns 是否存储成功
 */
export function setLocal(key: string, value: any): boolean {
  try {
    localStorage.setItem(key, JSON.stringify(value))
    return true
  } catch (error) {
    // 隐私模式下 localStorage 可能不可用
    console.warn('存储数据失败（可能是隐私模式）：', error)
    return false
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
    // 隐私模式下 localStorage 可能不可用，或者 JSON 解析失败
    console.warn('读取数据失败：', error)
    return defaultValue ?? null
  }
}

/**
 * localStorage 删除
 * @param key 键名
 */
export function removeLocal(key: string): void {
  try {
    localStorage.removeItem(key)
  } catch (error) {
    console.warn('删除数据失败：', error)
  }
}

/**
 * localStorage 清空
 */
export function clearLocal(): void {
  try {
    localStorage.clear()
  } catch (error) {
    console.warn('清空数据失败：', error)
  }
}

/**
 * sessionStorage 存储
 * @param key 键名
 * @param value 值
 * @returns 是否存储成功
 */
export function setSession(key: string, value: any): boolean {
  try {
    sessionStorage.setItem(key, JSON.stringify(value))
    return true
  } catch (error) {
    console.warn('存储数据失败（可能是隐私模式）：', error)
    return false
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
    console.warn('读取数据失败：', error)
    return defaultValue ?? null
  }
}

/**
 * sessionStorage 删除
 * @param key 键名
 */
export function removeSession(key: string): void {
  try {
    sessionStorage.removeItem(key)
  } catch (error) {
    console.warn('删除数据失败：', error)
  }
}

/**
 * sessionStorage 清空
 */
export function clearSession(): void {
  try {
    sessionStorage.clear()
  } catch (error) {
    console.warn('清空数据失败：', error)
  }
}
