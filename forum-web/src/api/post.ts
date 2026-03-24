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
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的帖子ID')
  }
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
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的帖子ID')
  }
  return request.delete(`/posts/${id}`)
}

// 置顶帖子
export function topPost(id: number, isTop: number): Promise<{ isTop: number; message: string }> {
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的帖子ID')
  }
  if (isTop !== 0 && isTop !== 1) {
    throw new Error('置顶状态值无效，必须是0或1')
  }
  return request.put(`/posts/${id}/top`, null, { params: { isTop } })
}

// 加精帖子
export function essencePost(id: number, isEssence: number): Promise<{ isEssence: number; message: string }> {
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的帖子ID')
  }
  if (isEssence !== 0 && isEssence !== 1) {
    throw new Error('加精状态值无效，必须是0或1')
  }
  return request.put(`/posts/${id}/essence`, null, { params: { isEssence } })
}

// 移动帖子
export function movePost(id: number, forumId: number): Promise<void> {
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的帖子ID')
  }
  if (!forumId || isNaN(forumId) || forumId <= 0) {
    throw new Error('无效的目标版块ID')
  }
  // 【修复】后端接口使用查询参数接收forumId，而非请求体
  return request.put(`/posts/${id}/move`, null, { params: { forumId } })
}

// 关闭帖子
export function closePost(id: number): Promise<void> {
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的帖子ID')
  }
  return request.put(`/posts/${id}/close`)
}

// 审核帖子（通过/拒绝）
export function auditPost(id: number, status: number, reason?: string): Promise<void> {
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的帖子ID')
  }
  // 【修复】状态值验证：后端定义 2-审核通过，3-审核拒绝
  if (status !== 2 && status !== 3) {
    throw new Error('审核状态值无效，必须是2(审核通过)或3(审核拒绝)')
  }
  // 拒绝时需要填写原因
  if (status === 3 && (!reason || reason.trim() === '')) {
    throw new Error('拒绝审核时必须填写原因')
  }
  // 后端接口使用查询参数接收status和reason，而非请求体
  const params: Record<string, any> = { status }
  if (reason) {
    params.reason = reason
  }
  return request.put(`/posts/${id}/audit`, null, { params })
}

// 获取热门帖子
export function getHotPosts(limit: number = 10): Promise<PostVO[]> {
  if (limit && (isNaN(limit) || limit <= 0 || limit > 100)) {
    throw new Error('limit参数必须在1-100之间')
  }
  return request.get('/posts/hot', { limit })
}

// 搜索帖子
export function searchPosts(params: { keyword: string; page: number; size: number }): Promise<PageResult<PostVO>> {
  const keyword = params.keyword?.trim()
  if (!keyword) {
    throw new Error('搜索关键词不能为空')
  }
  if (keyword.length > 50) {
    throw new Error('搜索关键词不能超过50个字符')
  }
  // 兼容后端分页参数
  const queryParams = {
    keyword,
    current: params.page || 1,
    size: Math.max(1, Math.min(100, params.size || 10))
  }
  return request.get('/posts/search', queryParams)
}

// 获取版块帖子
export function getPostsByForum(forumId: number, params: PostQueryDTO): Promise<PageResult<PostVO>> {
  if (!forumId || isNaN(forumId) || forumId <= 0) {
    throw new Error('无效的版块ID')
  }
  // 兼容后端分页参数
  const queryParams = {
    current: params.page || params.current || 1,
    size: Math.max(1, Math.min(100, params.size || 10))
  }
  return request.get(`/posts/forum/${forumId}`, queryParams)
}

// 点赞帖子（toggle模式）
export function likePost(id: number): Promise<{ isLike: boolean; isLiked?: boolean; message: string }> {
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的帖子ID')
  }
  return request.post(`/posts/${id}/like`)
}

// 收藏帖子（toggle模式）
export function collectPost(id: number): Promise<{ isCollect: boolean; isCollected?: boolean; message: string }> {
  if (!id || isNaN(id) || id <= 0) {
    throw new Error('无效的帖子ID')
  }
  return request.post(`/posts/${id}/collect`)
}

// 获取用户帖子
export function getUserPosts(userId: number, params: { page: number; size: number }): Promise<PageResult<any>> {
  if (!userId || isNaN(userId) || userId <= 0) {
    throw new Error('无效的用户ID')
  }
  // 兼容后端分页参数
  const queryParams = {
    current: params.page || 1,
    size: Math.max(1, Math.min(100, params.size || 10))
  }
  return request.get(`/users/${userId}/posts`, queryParams)
}
