import { request, PageResult } from './request'
import type { PostVO, PostDetailVO, PostQueryDTO, PostCreateDTO } from '@/types'

// 获取帖子列表
export function getPostList(params: PostQueryDTO): Promise<PageResult<PostVO>> {
  // 兼容后端分页参数
  const queryParams = {
    current: params.page || params.current,
    size: params.size,
    forumId: params.forumId,
    userId: params.userId,
    keyword: params.keyword,
    status: params.status,  // 添加status参数，允许前端传递
    sortType: params.sortType,
    isTop: params.isTop,
    isEssence: params.isEssence,
    type: params.type
  }
  return request.get('/posts', queryParams)
}

// 获取帖子详情
export function getPostById(id: number): Promise<PostDetailVO> {
  return request.get(`/posts/${id}`)
}

// 获取帖子详情（别名）
export const getPostDetail = getPostById

// 发布帖子
export function createPost(data: PostCreateDTO): Promise<number> {
  // 参数验证
  if (!data) {
    throw new Error('帖子数据不能为空')
  }
  // 版块ID验证
  if (!data.forumId || isNaN(data.forumId) || data.forumId <= 0) {
    throw new Error('请选择有效的版块')
  }
  // 标题验证
  const title = data.title?.trim()
  if (!title) {
    throw new Error('帖子标题不能为空')
  }
  if (title.length < 5 || title.length > 100) {
    throw new Error('帖子标题长度为5-100个字符')
  }
  // 内容验证
  const content = data.content?.trim()
  if (!content) {
    throw new Error('帖子内容不能为空')
  }
  if (content.length < 10) {
    throw new Error('帖子内容至少10个字符')
  }
  return request.post('/posts', { ...data, title, content })
}

// 编辑帖子
export function updatePost(id: number, data: Partial<PostCreateDTO>): Promise<void> {
  // ID验证
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的帖子ID')
  }
  if (!data || Object.keys(data).length === 0) {
    throw new Error('更新数据不能为空')
  }
  // 标题验证（如果提供）
  if (data.title !== undefined) {
    const title = data.title.trim()
    if (!title) {
      throw new Error('帖子标题不能为空')
    }
    if (title.length < 5 || title.length > 100) {
      throw new Error('帖子标题长度为5-100个字符')
    }
    data = { ...data, title }
  }
  // 内容验证（如果提供）
  if (data.content !== undefined) {
    const content = data.content.trim()
    if (!content) {
      throw new Error('帖子内容不能为空')
    }
    if (content.length < 10) {
      throw new Error('帖子内容至少10个字符')
    }
    data = { ...data, content }
  }
  return request.put(`/posts/${id}`, data)
}

// 删除帖子
export function deletePost(id: number): Promise<void> {
  return request.delete(`/posts/${id}`)
}

// 置顶帖子
export function topPost(id: number, isTop: number): Promise<{ isTop: number; message: string }> {
  return request.put(`/posts/${id}/top`, null, { params: { isTop } })
}

// 加精帖子
export function essencePost(id: number, isEssence: number): Promise<{ isEssence: number; message: string }> {
  return request.put(`/posts/${id}/essence`, null, { params: { isEssence } })
}

// 移动帖子
export function movePost(id: number, forumId: number): Promise<void> {
  return request.put(`/posts/${id}/move`, { forumId })
}

// 关闭帖子
export function closePost(id: number): Promise<void> {
  return request.put(`/posts/${id}/close`)
}

// 审核帖子（通过/拒绝）
export function auditPost(id: number, status: number, reason?: string): Promise<void> {
  return request.put(`/posts/${id}/audit`, { status, reason })
}

// 获取热门帖子
export function getHotPosts(limit: number = 10): Promise<PostVO[]> {
  return request.get('/posts/hot', { limit })
}

// 搜索帖子
export function searchPosts(params: { keyword: string; page: number; size: number }): Promise<PageResult<PostVO>> {
  // 兼容后端分页参数
  const queryParams = {
    keyword: params.keyword,
    current: params.page,
    size: params.size
  }
  return request.get('/posts/search', queryParams)
}

// 获取版块帖子
export function getPostsByForum(forumId: number, params: PostQueryDTO): Promise<PageResult<PostVO>> {
  // 兼容后端分页参数
  const queryParams = {
    current: params.page || params.current,
    size: params.size
  }
  return request.get(`/posts/forum/${forumId}`, queryParams)
}

// 点赞帖子（toggle模式）
export function likePost(id: number): Promise<{ isLike: boolean; message: string }> {
  return request.post(`/posts/${id}/like`)
}

// 收藏帖子（toggle模式）
export function collectPost(id: number): Promise<{ isCollect: boolean; message: string }> {
  return request.post(`/posts/${id}/collect`)
}

// 获取用户帖子
export function getUserPosts(userId: number, params: { page: number; size: number }): Promise<PageResult<any>> {
  return request.get(`/users/${userId}/posts`, params)
}
