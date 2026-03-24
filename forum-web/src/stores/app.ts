import { defineStore } from 'pinia'
import { ref } from 'vue'

// 安全的localStorage读取函数
function safeGetTheme(): 'light' | 'dark' {
  try {
    const theme = localStorage.getItem('theme')
    if (theme === 'light' || theme === 'dark') {
      return theme
    }
  } catch (e) {
    console.warn('localStorage访问失败:', e)
  }
  return 'light'
}

// 安全的localStorage写入函数
function safeSetTheme(theme: 'light' | 'dark'): boolean {
  try {
    localStorage.setItem('theme', theme)
    return true
  } catch (e) {
    console.warn('localStorage写入失败:', e)
    return false
  }
}

export const useAppStore = defineStore('app', () => {
  // 侧边栏状态
  const sidebarCollapsed = ref(false)

  // 设备类型
  const device = ref<'desktop' | 'mobile'>('desktop')

  // 主题 - 使用安全的localStorage读取方法
  const theme = ref<'light' | 'dark'>(safeGetTheme())

  // 初始化时应用主题
  if (typeof window !== 'undefined') {
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  // 切换侧边栏
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 设置设备类型
  function setDevice(type: 'desktop' | 'mobile') {
    device.value = type
    if (type === 'mobile') {
      sidebarCollapsed.value = true
    }
  }

  // 切换主题 - 使用安全的localStorage写入方法
  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    safeSetTheme(theme.value)
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  return {
    sidebarCollapsed,
    device,
    theme,
    toggleSidebar,
    setDevice,
    toggleTheme
  }
})
