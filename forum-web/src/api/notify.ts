import { request, PageResult } from './request'

export interface NoticeVO {
  id: number
  title: string
  content: string
  type: number
  status: number
  publisherId: number
  publisherName: string
  isRead: boolean
  readTime: string
  createTime: string
  updateTime: string
}

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
  return request.post('/notices', data)
}

export function updateNotice(id: number, data: NoticeUpdateDTO): Promise<boolean> {
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
