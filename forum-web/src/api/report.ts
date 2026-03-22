import { request, PageResult } from './request'

export interface ReportVO {
  id: number
  reportType: number
  reasonType: number
  reason: string
  targetId: number
  targetTitle: string
  reporterId: number
  reporterName: string
  status: number
  result: number
  handlerId: number
  handlerName: string
  handleTime: string
  createTime: string
}

export interface ReportCreateDTO {
  reportType: number
  targetId: number
  reasonType: number
  reason?: string
}

export interface ReportHandleDTO {
  result: number
  handleRemark?: string
  action?: number
  banDays?: number
}

export interface ReportQueryDTO {
  current: number
  size: number
  status?: number
  reportType?: number
  reasonType?: number
}

export function submitReport(data: ReportCreateDTO): Promise<number> {
  return request.post('/reports', data)
}

export function getReportList(params: ReportQueryDTO): Promise<PageResult<ReportVO>> {
  return request.get('/reports', params)
}

export function getReportDetail(id: number): Promise<ReportVO> {
  return request.get(`/reports/${id}`)
}

export function handleReport(id: number, data: ReportHandleDTO): Promise<boolean> {
  return request.put(`/reports/${id}/handle`, data)
}

export function getPendingCount(): Promise<{pendingReportCount: number; pendingApproveCount: number; totalPendingCount: number}> {
  return request.get('/reports/pending-count')
}

export function banUser(data: { userId: number; banDays: number; reason: string }): Promise<number> {
  return request.post('/reports/ban', data)
}

export function unbanUser(userId: number, reason?: string): Promise<boolean> {
  return request.delete(`/reports/ban/${userId}`, { reason })
}
