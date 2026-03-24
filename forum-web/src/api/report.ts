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

// ==================== 审核相关接口 ====================

export interface ApproveVO {
  id: number
  contentType: number      // 内容类型：1-帖子 2-评论
  contentId: number        // 内容ID
  userId: number           // 提交用户ID
  userName: string         // 提交用户名
  title: string            // 标题（帖子）
  content: string          // 内容摘要
  status: number           // 审核状态：0-待审核 1-通过 2-拒绝
  auditorId: number        // 审核人ID
  auditorName: string      // 审核人名称
  auditTime: string        // 审核时间
  rejectReason: string     // 拒绝原因
  createTime: string       // 提交时间
}

export interface ApproveQueryDTO {
  page?: number
  current?: number
  size: number
  status?: number
  contentType?: number
  userId?: number
}

export interface ApproveHandleDTO {
  status: number           // 审核状态：1-通过 2-拒绝
  rejectReason?: string    // 拒绝原因
}

/**
 * 获取待审核列表
 */
export function getApproveList(params: ApproveQueryDTO): Promise<PageResult<ApproveVO>> {
  const queryParams = {
    current: params.current || params.page || 1,
    size: Math.max(1, Math.min(100, params.size || 10)),
    status: params.status,
    contentType: params.contentType,
    userId: params.userId
  }
  return request.get('/reports/approve', queryParams)
}

/**
 * 审核处理（通过/驳回）
 */
export function approve(id: number, data: ApproveHandleDTO): Promise<boolean> {
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的审核ID')
  }
  if (!data) {
    throw new Error('审核数据不能为空')
  }
  // 审核状态验证：1-通过，2-拒绝
  if (![1, 2].includes(data.status)) {
    throw new Error('审核状态值无效，必须是1(通过)或2(拒绝)')
  }
  // 拒绝时需要填写原因
  if (data.status === 2 && !data.rejectReason?.trim()) {
    throw new Error('拒绝时必须填写拒绝原因')
  }
  return request.put(`/reports/approve/${id}`, data)
}

/**
 * 获取审核详情
 */
export function getApproveDetail(id: number): Promise<ApproveVO> {
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的审核ID')
  }
  return request.get(`/reports/approve/${id}`)
}

// ==================== 禁言相关接口 ====================

export interface UserBanVO {
  id: number
  userId: number
  userName: string
  banDays: number
  reason: string
  operatorId: number
  operatorName: string
  startTime: string
  endTime: string
  status: number           // 禁言状态：0-已解除 1-生效中 2-已过期
  unbanTime: string        // 解除时间
  unbanOperatorId: number  // 解除操作人ID
  unbanReason: string      // 解除原因
  createTime: string
}

/**
 * 获取禁言列表
 */
export function getBanList(params: { page: number; size: number; userId?: number; status?: number }): Promise<PageResult<UserBanVO>> {
  const queryParams = {
    current: params.page || 1,
    size: Math.max(1, Math.min(100, params.size || 10)),
    userId: params.userId,
    status: params.status
  }
  return request.get('/reports/ban', queryParams)
}

/**
 * 获取用户禁言状态
 */
export function getUserBanStatus(userId: number): Promise<UserBanVO | null> {
  if (!userId || isNaN(userId) || userId <= 0) {
    throw new Error('无效的用户ID')
  }
  return request.get(`/reports/ban/user/${userId}`)
}

/**
 * 获取用户禁言历史
 */
export function getBanHistory(userId: number): Promise<UserBanVO[]> {
  if (!userId || isNaN(userId) || userId <= 0) {
    throw new Error('无效的用户ID')
  }
  return request.get(`/reports/ban/history/${userId}`)
}
