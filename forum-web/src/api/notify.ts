import { request, PageResult } from './request'
import type { NoticeVO } from '@/types'

export interface NoticeCreateDTO {
  title: string
  content: string
  type: number
  status?: number
}

export interface NoticeUpdateDTO {
  title?: string
  content?: string
  type?: number
  status?: number
}

export interface NoticeQueryDTO {
  current: number
  size: number
  type?: number
  status?: number
}

export function getNoticeList(params: NoticeQueryDTO): Promise<PageResult<NoticeVO>> {
  return request.get('/notices', params)
}

export function getNoticeDetail(id: number): Promise<NoticeVO> {
  return request.get(`/notices/${id}`)
}

export function createNotice(data: NoticeCreateDTO): Promise<number> {
  // 参数验证
  if (!data) {
    throw new Error('公告数据不能为空')
  }
  const title = data.title?.trim()
  if (!title) {
    throw new Error('公告标题不能为空')
  }
  if (title.length < 2 || title.length > 100) {
    throw new Error('公告标题长度为2-100个字符')
  }
  const content = data.content?.trim()
  if (!content) {
    throw new Error('公告内容不能为空')
  }
  if (content.length < 2 || content.length > 500) {
    throw new Error('公告内容长度为2-500个字符')
  }
  return request.post('/notices', { ...data, title, content })
}

export function updateNotice(id: number, data: NoticeUpdateDTO): Promise<boolean> {
  // ID验证
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的公告ID')
  }
  if (!data || Object.keys(data).length === 0) {
    throw new Error('更新数据不能为空')
  }
  // 标题验证（如果提供）
  if (data.title !== undefined) {
    const title = data.title.trim()
    if (!title) {
      throw new Error('公告标题不能为空')
    }
    if (title.length < 2 || title.length > 100) {
      throw new Error('公告标题长度为2-100个字符')
    }
    data = { ...data, title }
  }
  // 内容验证（如果提供）
  if (data.content !== undefined) {
    const content = data.content.trim()
    if (!content) {
      throw new Error('公告内容不能为空')
    }
    if (content.length < 2 || content.length > 500) {
      throw new Error('公告内容长度为2-500个字符')
    }
    data = { ...data, content }
  }
  return request.put(`/notices/${id}`, data)
}

export function deleteNotice(id: number): Promise<boolean> {
  return request.delete(`/notices/${id}`)
}

export function getUnreadCount(): Promise<{ count: number }> {
  return request.get('/notices/unread')
}

export function markAsRead(id: number): Promise<boolean> {
  return request.post(`/notices/${id}/read`)
}

export function markAllAsRead(): Promise<boolean> {
  return request.post('/notices/read/all')
}
