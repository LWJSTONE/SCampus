import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏状态
  const sidebarCollapsed = ref(false)

  // 设备类型
  const device = ref<'desktop' | 'mobile'>('desktop')

  // 主题 - 从localStorage读取持久化的值
  const theme = ref<'light' | 'dark'>(
    (localStorage.getItem('theme') as 'light' | 'dark') || 'light'
  )

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

  // 切换主题 - 同时持久化到localStorage
  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    localStorage.setItem('theme', theme.value)
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
