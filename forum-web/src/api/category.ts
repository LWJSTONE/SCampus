/**
 * 版块相关 API
 * 处理分类和版块的查询、管理等接口
 */
import { request } from './request'
import type { CategoryVO, ForumVO } from '@/types'

/**
 * 获取所有分类（带版块）
 */
export function getCategoryList() {
  return request.get<CategoryVO[]>('/categories')
}

/**
 * 获取分类详情
 * @param id 分类ID
 */
export function getCategoryDetail(id: number) {
  return request.get<CategoryVO>(`/categories/${id}`)
}

/**
 * 创建分类（管理员）
 * @param data 分类数据
 */
export function createCategory(data: Partial<CategoryVO>) {
  return request.post('/categories', data)
}

/**
 * 更新分类（管理员）
 * @param id 分类ID
 * @param data 分类数据
 */
export function updateCategory(id: number, data: Partial<CategoryVO>) {
  return request.put(`/categories/${id}`, data)
}

/**
 * 删除分类（管理员）
 * @param id 分类ID
 */
export function deleteCategory(id: number) {
  return request.delete(`/categories/${id}`)
}

/**
 * 获取版块列表
 * @param categoryId 分类ID（可选）
 */
export function getForumList(categoryId?: number) {
  return request.get<ForumVO[]>('/forums', categoryId ? { categoryId } : undefined)
}

/**
 * 获取版块详情
 * @param id 版块ID
 */
export function getForumDetail(id: number) {
  return request.get<ForumVO>(`/forums/${id}`)
}

/**
 * 创建版块（管理员）
 * @param data 版块数据
 */
export function createForum(data: Partial<ForumVO>) {
  return request.post('/forums', data)
}

/**
 * 更新版块（管理员）
 * @param id 版块ID
 * @param data 版块数据
 */
export function updateForum(id: number, data: Partial<ForumVO>) {
  return request.put(`/forums/${id}`, data)
}

/**
 * 删除版块（管理员）
 * @param id 版块ID
 */
export function deleteForum(id: number) {
  return request.delete(`/forums/${id}`)
}

/**
 * 设置版主
 * @param forumId 版块ID
 * @param userId 用户ID
 */
export function setModerator(forumId: number, userId: number) {
  return request.post(`/forums/${forumId}/moderator`, { userId })
}

/**
 * 移除版主
 * @param forumId 版块ID
 * @param userId 用户ID
 */
export function removeModerator(forumId: number, userId: number) {
  return request.delete(`/forums/${forumId}/moderator/${userId}`)
}
