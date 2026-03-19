import request, { PageResult } from './request'
import type { UserVO, UserDetailVO, UserQueryDTO, UserUpdateDTO } from '@/types'

// 获取用户列表
export function getUserList(params: UserQueryDTO): Promise<PageResult<UserVO>> {
  return request.get('/users', params)
}

// 获取用户详情
export function getUserById(id: number): Promise<UserDetailVO> {
  return request.get(`/users/${id}`)
}

// 更新用户信息
export function updateUser(id: number, data: UserUpdateDTO): Promise<void> {
  return request.put(`/users/${id}`, data)
}

// 更新头像
export function updateAvatar(id: number, avatar: string): Promise<void> {
  return request.put(`/users/${id}/avatar`, { avatar })
}

// 修改密码
export function updatePassword(id: number, data: { oldPassword: string; newPassword: string }): Promise<void> {
  return request.put(`/users/${id}/password`, data)
}

// 获取粉丝列表
export function getFollowers(id: number, params: { page: number; size: number }): Promise<PageResult<UserVO>> {
  return request.get(`/users/${id}/followers`, params)
}

// 获取关注列表
export function getFollowing(id: number, params: { page: number; size: number }): Promise<PageResult<UserVO>> {
  return request.get(`/users/${id}/following`, params)
}

// 关注用户
export function followUser(id: number): Promise<void> {
  return request.post(`/users/${id}/follow`)
}

// 取消关注
export function unfollowUser(id: number): Promise<void> {
  return request.delete(`/users/${id}/follow`)
}

// 获取当前登录用户信息
export function getCurrentUserInfo(): Promise<UserDetailVO> {
  return request.get('/users/me')
}

// 更新用户状态
export function updateUserStatus(id: number, status: number): Promise<void> {
  return request.put(`/users/${id}/status`, { status })
}

// 获取用户帖子
export function getUserPosts(id: number, params: { page: number; size: number }): Promise<PageResult<any>> {
  return request.get(`/users/${id}/posts`, params)
}

// 获取用户评论
export function getUserComments(id: number, params: { page: number; size: number }): Promise<PageResult<any>> {
  return request.get(`/users/${id}/comments`, params)
}

// 获取用户收藏
export function getUserCollections(id: number, params: { page: number; size: number }): Promise<PageResult<any>> {
  return request.get(`/users/${id}/collections`, params)
}
