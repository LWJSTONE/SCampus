import request, { PageResult } from './request'
import type { PostVO, PostDetailVO, PostQueryDTO, PostCreateDTO } from '@/types'

// 获取帖子列表
export function getPostList(params: PostQueryDTO): Promise<PageResult<PostVO>> {
  return request.get('/posts', params)
}

// 获取帖子详情
export function getPostById(id: number): Promise<PostDetailVO> {
  return request.get(`/posts/${id}`)
}

// 发布帖子
export function createPost(data: PostCreateDTO): Promise<number> {
  return request.post('/posts', data)
}

// 编辑帖子
export function updatePost(id: number, data: Partial<PostCreateDTO>): Promise<void> {
  return request.put(`/posts/${id}`, data)
}

// 删除帖子
export function deletePost(id: number): Promise<void> {
  return request.delete(`/posts/${id}`)
}

// 置顶帖子
export function topPost(id: number, top: boolean): Promise<void> {
  return request.put(`/posts/${id}/top`, { top })
}

// 加精帖子
export function essencePost(id: number, essence: boolean): Promise<void> {
  return request.put(`/posts/${id}/essence`, { essence })
}

// 移动帖子
export function movePost(id: number, forumId: number): Promise<void> {
  return request.put(`/posts/${id}/move`, { forumId })
}

// 关闭帖子
export function closePost(id: number): Promise<void> {
  return request.put(`/posts/${id}/close`)
}

// 获取热门帖子
export function getHotPosts(limit: number = 10): Promise<PostVO[]> {
  return request.get('/posts/hot', { limit })
}

// 搜索帖子
export function searchPosts(params: { keyword: string; page: number; size: number }): Promise<PageResult<PostVO>> {
  return request.get('/posts/search', params)
}

// 获取版块帖子
export function getPostsByForum(forumId: number, params: PostQueryDTO): Promise<PageResult<PostVO>> {
  return request.get(`/posts/forum/${forumId}`, params)
}

// 点赞帖子
export function likePost(id: number): Promise<{ liked: boolean; likeCount: number }> {
  return request.post(`/posts/${id}/like`)
}

// 收藏帖子
export function collectPost(id: number): Promise<{ collected: boolean; collectCount: number }> {
  return request.post(`/posts/${id}/collect`)
}
