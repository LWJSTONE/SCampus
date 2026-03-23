import { request, PageResult } from './request'
import type { UserVO, UserDetailVO, UserQueryDTO, UserUpdateDTO } from '@/types'

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
  return request.get(`/users/${id}`)
}

// 更新用户信息
export function updateUser(id: number, data: UserUpdateDTO): Promise<boolean> {
  return request.put(`/users/${id}`, data)
}

// 更新头像
export function updateAvatar(id: number, avatar: string): Promise<boolean> {
  return request.put(`/users/${id}/avatar`, { avatar })
}

// 修改密码
export function updatePassword(id: number, data: { oldPassword: string; newPassword: string; confirmPassword?: string }): Promise<boolean> {
  return request.put(`/users/${id}/password`, data)
}

// 获取粉丝列表
export function getFollowers(id: number, params: { page: number; size: number }): Promise<PageResult<UserVO>> {
  // 兼容后端分页参数
  const queryParams = {
    current: params.page,
    size: params.size
  }
  return request.get(`/users/${id}/followers`, queryParams)
}

// 获取关注列表
export function getFollowing(id: number, params: { page: number; size: number }): Promise<PageResult<UserVO>> {
  // 兼容后端分页参数
  const queryParams = {
    current: params.page,
    size: params.size
  }
  return request.get(`/users/${id}/following`, queryParams)
}

// 关注用户
export function followUser(id: number): Promise<boolean> {
  return request.post(`/users/${id}/follow`)
}

// 取消关注
export function unfollowUser(id: number): Promise<boolean> {
  return request.delete(`/users/${id}/follow`)
}

// 获取当前登录用户信息
export function getCurrentUserInfo(): Promise<UserDetailVO> {
  return request.get('/users/me')
}

// 获取用户帖子
export function getUserPosts(id: number, params: { page: number; size: number }): Promise<PageResult<any>> {
  // 兼容后端分页参数
  const queryParams = {
    current: params.page,
    size: params.size
  }
  return request.get(`/users/${id}/posts`, queryParams)
}

// 获取用户评论
export function getUserComments(id: number, params: { page: number; size: number }): Promise<PageResult<any>> {
  // 兼容后端分页参数
  const queryParams = {
    current: params.page,
    size: params.size
  }
  return request.get(`/users/${id}/comments`, queryParams)
}

// 获取用户收藏
export function getUserCollections(id: number, params: { page: number; size: number }): Promise<PageResult<any>> {
  // 兼容后端分页参数
  const queryParams = {
    current: params.page,
    size: params.size
  }
  return request.get(`/users/${id}/collections`, queryParams)
}

// 更新用户状态（管理员）
export function updateUserStatus(id: number, status: number): Promise<boolean> {
  return request.put(`/users/${id}/status`, null, { params: { status } })
}
