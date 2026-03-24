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
  /** 当前页码 (兼容后端分页参数) */
  current?: number
  /** 页码 (前端统一使用page) */
  page?: number
  /** 每页大小 */
  size: number
  status?: number
  reportType?: number
  reasonType?: number
}

export function submitReport(data: ReportCreateDTO): Promise<number> {
  // 参数验证
  if (!data) {
    throw new Error('举报数据不能为空')
  }
  // 举报类型验证 (1-帖子, 2-评论, 3-用户)
  if (!data.reportType || ![1, 2, 3].includes(data.reportType)) {
    throw new Error('无效的举报类型')
  }
  // 目标ID验证
  if (!data.targetId || isNaN(data.targetId) || data.targetId <= 0) {
    throw new Error('无效的目标ID')
  }
  // 原因类型验证
  if (!data.reasonType || data.reasonType < 1) {
    throw new Error('请选择举报原因')
  }
  return request.post('/reports', data)
}

export function getReportList(params: ReportQueryDTO): Promise<PageResult<ReportVO>> {
  // 兼容后端分页参数：将 page 转换为 current
  const queryParams = {
    ...params,
    current: params.current || params.page || 1,
  }
  return request.get('/reports', queryParams)
}

export function getReportDetail(id: number): Promise<ReportVO> {
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的举报ID')
  }
  return request.get(`/reports/${id}`)
}

export function handleReport(id: number, data: ReportHandleDTO): Promise<boolean> {
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的举报ID')
  }
  if (!data) {
    throw new Error('处理数据不能为空')
  }
  // 处理结果验证：1-通过，2-拒绝
  if (!data.result || ![1, 2].includes(data.result)) {
    throw new Error('处理结果值无效，必须是1(通过)或2(拒绝)')
  }
  return request.put(`/reports/${id}/handle`, data)
}

export function getPendingCount(): Promise<{pendingReportCount: number; pendingApproveCount: number; totalPendingCount: number}> {
  return request.get('/reports/pending-count')
}

export function banUser(data: { userId: number; banDays: number; reason: string }): Promise<number> {
  if (!data) {
    throw new Error('封禁数据不能为空')
  }
  if (!data.userId || isNaN(data.userId) || data.userId <= 0) {
    throw new Error('无效的用户ID')
  }
  if (!data.banDays || isNaN(data.banDays) || data.banDays <= 0) {
    throw new Error('封禁天数必须是正整数')
  }
  const reason = data.reason?.trim()
  if (!reason) {
    throw new Error('封禁原因不能为空')
  }
  return request.post('/reports/ban', { ...data, reason })
}

export function unbanUser(userId: number, reason?: string): Promise<boolean> {
  if (!userId || isNaN(userId) || userId <= 0) {
    throw new Error('无效的用户ID')
  }
  return request.delete(`/reports/ban/${userId}`, { reason })
}
