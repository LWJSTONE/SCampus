import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

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
  baseURL: '/api/v1',
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

// 订阅Token刷新完成事件
function subscribeTokenRefresh(callback: (token: string) => void) {
  refreshSubscribers.push(callback)
}

// 通知所有订阅者Token已刷新
function onRefreshed(token: string) {
  refreshSubscribers.forEach(callback => callback(token))
  refreshSubscribers = []
}

// Token刷新失败，清除订阅队列
function onRefreshFailed() {
  refreshSubscribers = []
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
            if (!isRefreshing) {
              isRefreshing = true
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
                isRefreshing = false
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
            // 调用刷新Token接口
            const response = await axios.post('/api/v1/auth/refresh', {
              refreshToken: refreshTokenValue
            })

            const { accessToken, refreshToken: newRefreshToken } = response.data.data || response.data

            // 更新store中的token
            userStore.token = accessToken
            userStore.refreshToken = newRefreshToken
            localStorage.setItem('token', accessToken)
            localStorage.setItem('refreshToken', newRefreshToken)

            // 通知所有等待的请求使用新Token
            onRefreshed(accessToken)

            // 使用新Token重试原请求
            originalRequest.headers.Authorization = `Bearer ${accessToken}`
            return service(originalRequest)
          } catch (refreshError) {
            // 刷新Token失败
            onRefreshFailed()
            userStore.clearAuth()

            ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              router.push({ name: 'Login' })
            }).catch(() => {
              // 用户取消
            })

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

  download(url: string, params?: any, filename?: string): void {
    service.get(url, { params, responseType: 'blob' }).then((response: any) => {
      const blob = new Blob([response.data])
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = filename || 'download'
      link.click()
      URL.revokeObjectURL(link.href)
    })
  }
}

// 导出单独的请求方法（向后兼容）
export const { get, post, put, delete: del } = request

export default service
