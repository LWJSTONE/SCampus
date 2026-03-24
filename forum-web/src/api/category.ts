/**
 * 版块相关 API
 * 处理分类和版块的查询、管理等接口
 */
import { request } from './request'
import type { CategoryVO, ForumVO } from '@/types'

/**
 * 获取所有分类（带版块）
 */
export function getCategoryList(): Promise<CategoryVO[]> {
  return request.get('/categories')
}

/**
 * 获取分类详情
 * @param id 分类ID
 */
export function getCategoryDetail(id: number): Promise<CategoryVO> {
  return request.get(`/categories/${id}`)
}

/**
 * 创建分类（管理员）
 * @param data 分类数据
 */
export function createCategory(data: Partial<CategoryVO>) {
  // 参数验证
  if (!data) {
    throw new Error('分类数据不能为空')
  }
  const name = data.name?.trim()
  if (!name) {
    throw new Error('分类名称不能为空')
  }
  if (name.length < 2 || name.length > 50) {
    throw new Error('分类名称长度为2-50个字符')
  }
  return request.post('/categories', { ...data, name })
}

/**
 * 更新分类（管理员）
 * @param id 分类ID
 * @param data 分类数据
 */
export function updateCategory(id: number, data: Partial<CategoryVO>) {
  // ID验证
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的分类ID')
  }
  if (!data || Object.keys(data).length === 0) {
    throw new Error('更新数据不能为空')
  }
  // 名称验证（如果提供）
  if (data.name !== undefined) {
    const name = data.name.trim()
    if (!name) {
      throw new Error('分类名称不能为空')
    }
    if (name.length < 2 || name.length > 50) {
      throw new Error('分类名称长度为2-50个字符')
    }
    data = { ...data, name }
  }
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
export function getForumList(categoryId?: number): Promise<ForumVO[]> {
  return request.get('/forums', categoryId ? { categoryId } : undefined)
}

/**
 * 获取版块详情
 * @param id 版块ID
 */
export function getForumDetail(id: number): Promise<ForumVO> {
  return request.get(`/forums/${id}`)
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
  return request.post(`/forums/${forumId}/moderators`, { userId })
}

/**
 * 移除版主
 * @param forumId 版块ID
 * @param userId 用户ID
 */
export function removeModerator(forumId: number, userId: number) {
  return request.delete(`/forums/${forumId}/moderators/${userId}`)
}
