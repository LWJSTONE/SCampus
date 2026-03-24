import { request } from './request'
import type { PageResult } from './request'
import type { UserVO, UserDetailVO, UserQueryDTO, UserUpdateDTO } from '@/types'
import { VALID_USER_STATUSES, USER_STATUS_TEXT } from '@/constants'

// 参数验证辅助函数
function validateId(id: number, paramName: string = 'id'): void {
  if (id === undefined || id === null || (typeof id === 'number' && (isNaN(id) || id <= 0))) {
    throw new Error(`无效的${paramName}: ${id}`)
  }
}

// 获取用户列表
export function getUserList(params: UserQueryDTO): Promise<PageResult<UserVO>> {
  // 兼容后端分页参数：将 page 转换为 current
  const queryParams = {
    ...params,
    current: params.page || params.current,
  }
  return request.get('/users', queryParams)
}

// 获取用户详情
export function getUserById(id: number): Promise<UserDetailVO> {
  validateId(id)
  return request.get(`/users/${id}`)
}

// 更新用户信息
export function updateUser(id: number, data: UserUpdateDTO): Promise<boolean> {
  validateId(id)
  if (!data || Object.keys(data).length === 0) {
    return Promise.resolve(false)
  }
  return request.put(`/users/${id}`, data)
}

// 更新头像
export function updateAvatar(id: number, avatar: string): Promise<boolean> {
  validateId(id)
  if (!avatar || typeof avatar !== 'string') {
    throw new Error('头像URL不能为空')
  }
  return request.put(`/users/${id}/avatar`, { avatar })
}

// 修改密码
export function updatePassword(id: number, data: { oldPassword: string; newPassword: string; confirmPassword?: string }): Promise<boolean> {
  validateId(id)
  if (!data?.oldPassword || !data?.newPassword) {
    throw new Error('旧密码和新密码不能为空')
  }
  if (data.oldPassword === data.newPassword) {
    throw new Error('新密码不能与旧密码相同')
  }
  // confirmPassword 只在前端验证使用，不需要传给后端
  const { confirmPassword, ...requestData } = data
  return request.put(`/users/${id}/password`, requestData)
}

// 获取粉丝列表
export function getFollowers(id: number, params: { page: number; size: number }): Promise<PageResult<UserVO>> {
  validateId(id)
  // 兼容后端分页参数
  const queryParams = {
    current: Math.max(1, params.page || 1),
    size: Math.max(1, Math.min(100, params.size || 10)) // 限制每页最大100条
  }
  return request.get(`/users/${id}/followers`, queryParams)
}

// 获取关注列表
export function getFollowing(id: number, params: { page: number; size: number }): Promise<PageResult<UserVO>> {
  validateId(id)
  // 兼容后端分页参数
  const queryParams = {
    current: Math.max(1, params.page || 1),
    size: Math.max(1, Math.min(100, params.size || 10))
  }
  return request.get(`/users/${id}/following`, queryParams)
}

// 关注用户
export function followUser(id: number): Promise<boolean> {
  validateId(id)
  return request.post(`/users/${id}/follow`)
}

// 取消关注
export function unfollowUser(id: number): Promise<boolean> {
  validateId(id)
  return request.delete(`/users/${id}/follow`)
}

// 获取当前登录用户信息
export function getCurrentUserInfo(): Promise<UserDetailVO> {
  return request.get('/users/me')
}

// 获取用户帖子
export function getUserPosts(id: number, params: { page: number; size: number }): Promise<PageResult<any>> {
  validateId(id)
  // 兼容后端分页参数
  const queryParams = {
    current: Math.max(1, params.page || 1),
    size: Math.max(1, Math.min(100, params.size || 10))
  }
  return request.get(`/users/${id}/posts`, queryParams)
}

// 获取用户评论
export function getUserComments(id: number, params: { page: number; size: number }): Promise<PageResult<any>> {
  validateId(id)
  // 兼容后端分页参数
  const queryParams = {
    current: Math.max(1, params.page || 1),
    size: Math.max(1, Math.min(100, params.size || 10))
  }
  return request.get(`/users/${id}/comments`, queryParams)
}

// 获取用户收藏
export function getUserCollections(id: number, params: { page: number; size: number }): Promise<PageResult<any>> {
  validateId(id)
  // 兼容后端分页参数
  const queryParams = {
    current: Math.max(1, params.page || 1),
    size: Math.max(1, Math.min(100, params.size || 10))
  }
  return request.get(`/users/${id}/collections`, queryParams)
}

// 更新用户状态（管理员）
export function updateUserStatus(id: number, status: number): Promise<boolean> {
  validateId(id)
  // 验证status值 - 使用常量定义
  if (!VALID_USER_STATUSES.includes(status)) {
    throw new Error(`无效的状态值: ${status}，有效值为: ${VALID_USER_STATUSES.map(s => `${s}(${USER_STATUS_TEXT[s]})`).join(', ')}`)
  }
  return request.put(`/users/${id}/status`, null, { params: { status } })
}
