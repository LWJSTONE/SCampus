import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// API基础URL配置
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1'

// 响应数据接口
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

// 分页结果接口
export interface PageResult<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
  list?: T[]
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// ============ Token刷新机制 ============
// 防止多个请求同时刷新Token
let isRefreshing = false
// 存储等待重试的请求队列
let refreshSubscribers: Array<(token: string) => void> = []
// 防止重复弹出登录过期提示
let hasShownExpiredDialog = false

// 订阅Token刷新完成事件
function subscribeTokenRefresh(callback: (token: string) => void) {
  refreshSubscribers.push(callback)
}

// 通知所有订阅者Token已刷新
function onRefreshed(token: string) {
  refreshSubscribers.forEach(callback => callback(token))
  refreshSubscribers = []
}

// Token刷新失败，清除订阅队列并拒绝所有等待的请求
function onRefreshFailed() {
  // 保存当前的订阅者队列引用
  const subscribers = [...refreshSubscribers]
  refreshSubscribers = []
  hasShownExpiredDialog = false
  // 返回订阅者数量，供调用者决定是否需要额外处理
  return subscribers.length
}

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const res = response.data

    // 文件下载直接返回
    if (response.config.responseType === 'blob') {
      return response as any
    }

    // 成功响应
    if (res.code === 200) {
      return res.data
    }

    // 业务错误
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  async (error) => {
    console.error('响应错误:', error)

    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (error.response) {
      const status = error.response.status

      switch (status) {
        case 401:
          // Token过期或未登录
          // 检查是否有refreshToken，以及是否已经重试过
          const userStore = useUserStore()
          const refreshTokenValue = userStore.refreshToken

          // 如果没有refreshToken或已经重试过，直接跳转登录
          if (!refreshTokenValue || originalRequest._retry) {
            // 刷新失败或没有refreshToken，跳转登录页
            // 防止重复弹出对话框
            if (!hasShownExpiredDialog) {
              hasShownExpiredDialog = true
              userStore.clearAuth()
              ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
                confirmButtonText: '重新登录',
                cancelButtonText: '取消',
                type: 'warning'
              }).then(() => {
                router.push({ name: 'Login' })
              }).catch(() => {
                // 用户取消
              }).finally(() => {
                hasShownExpiredDialog = false
              })
            }
            return Promise.reject(error)
          }

          // 标记此请求已重试
          originalRequest._retry = true

          // 如果正在刷新Token，将请求加入队列等待
          if (isRefreshing) {
            return new Promise((resolve) => {
              subscribeTokenRefresh((newToken: string) => {
                // 使用新Token重新发起请求
                originalRequest.headers.Authorization = `Bearer ${newToken}`
                resolve(service(originalRequest))
              })
            })
          }

          // 开始刷新Token
          isRefreshing = true

          try {
            // 调用刷新Token接口 - 使用完整的URL，避免baseURL问题
            const response = await axios.post(`${BASE_URL}/auth/refresh`, {
              refreshToken: refreshTokenValue
            })

            const responseData = response.data?.data || response.data
            const accessToken = responseData.accessToken || responseData.access_token
            const newRefreshToken = responseData.refreshToken || responseData.refresh_token

            if (!accessToken) {
              throw new Error('刷新Token失败：未获取到新的访问令牌')
            }

            // 更新store中的token
            userStore.token = accessToken
            userStore.refreshToken = newRefreshToken || refreshTokenValue
            localStorage.setItem('token', accessToken)
            if (newRefreshToken) {
              localStorage.setItem('refreshToken', newRefreshToken)
            }

            // 通知所有等待的请求使用新Token
            onRefreshed(accessToken)

            // 使用新Token重试原请求
            originalRequest.headers.Authorization = `Bearer ${accessToken}`
            return service(originalRequest)
          } catch (refreshError) {
            // 刷新Token失败
            onRefreshFailed()
            userStore.clearAuth()

            // 防止重复弹出对话框
            if (!hasShownExpiredDialog) {
              hasShownExpiredDialog = true
              ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
                confirmButtonText: '重新登录',
                cancelButtonText: '取消',
                type: 'warning'
              }).then(() => {
                router.push({ name: 'Login' })
              }).catch(() => {
                // 用户取消
              }).finally(() => {
                hasShownExpiredDialog = false
              })
            }

            return Promise.reject(refreshError)
          } finally {
            isRefreshing = false
          }

        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.message || '请求失败')
      }
    } else if (error.message.includes('timeout')) {
      ElMessage.error('请求超时')
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

// 封装请求方法
export const request = {
  get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, { params, ...config })
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },

  delete<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, { params, ...config })
  },

  upload<T = any>(url: string, file: File, onProgress?: (percent: number) => void): Promise<T> {
    const formData = new FormData()
    formData.append('file', file)
    return service.post(url, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total && onProgress) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(percent)
        }
      }
    })
  },

  download(url: string, params?: any, filename?: string): Promise<void> {
    return service.get(url, { params, responseType: 'blob' }).then((response: any) => {
      const blob = new Blob([response.data])
      // 尝试从响应头获取文件名
      const contentDisposition = response.headers?.['content-disposition']
      let downloadFilename = filename || 'download'
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
        if (filenameMatch && filenameMatch[1]) {
          downloadFilename = decodeURIComponent(filenameMatch[1].replace(/['"]/g, ''))
        }
      }
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = downloadFilename
      link.click()
      URL.revokeObjectURL(link.href)
    }).catch((error) => {
      console.error('文件下载失败:', error)
      ElMessage.error('文件下载失败')
      throw error
    })
  }
}

// 导出单独的请求方法（向后兼容）
export const { get, post, put, delete: del } = request

export default service
