/**
 * 评论相关 API
 * 处理评论的增删改查、点赞等接口
 */
import { request, PageResult } from './request'
import type { CommentVO, CommentCreateDTO, PageQuery } from '@/types'

/**
 * 评论查询参数
 */
interface CommentQueryParams extends PageQuery {
  postId: number
  orderBy?: string
}

/**
 * 获取帖子的评论（树形结构）
 * @param postId 帖子ID
 * @param params 分页参数
 */
export function getPostComments(postId: number, params: PageQuery): Promise<PageResult<CommentVO>> {
  // 兼容后端分页参数，使用nullish coalescing避免0值问题
  const queryParams = {
    current: params.page ?? params.current ?? 1,
    size: params.size ?? 10
  }
  return request.get(`/comments/post/${postId}`, queryParams)
}

/**
 * 创建评论
 * @param data 评论数据
 */
export function createComment(data: CommentCreateDTO): Promise<number> {
  return request.post('/comments', data)
}

/**
 * 删除评论
 * @param id 评论ID
 */
export function deleteComment(id: number): Promise<boolean> {
  return request.delete(`/comments/${id}`)
}

/**
 * 点赞评论（toggle模式）
 * @param id 评论ID
 */
export function likeComment(id: number): Promise<{ isLike: boolean; message: string; likeCount?: number }> {
  return request.post(`/comments/${id}/like`)
}

/**
 * 获取评论的回复列表
 * @param id 评论ID
 * @param params 分页参数
 */
export function getCommentReplies(id: number, params: PageQuery): Promise<PageResult<CommentVO>> {
  return request.get(`/comments/${id}/replies`, params)
}

/**
 * 获取评论详情
 * @param id 评论ID
 */
export function getCommentDetail(id: number): Promise<CommentVO> {
  return request.get(`/comments/${id}`)
}

/**
 * 获取用户的评论
 * @param userId 用户ID
 * @param params 分页参数
 */
export function getUserComments(userId: number, params: PageQuery): Promise<PageResult<CommentVO>> {
  return request.get(`/comments/user/${userId}`, params)
}

/**
 * 管理员审核评论
 * @param id 评论ID
 * @param status 状态
 */
export function auditComment(id: number, status: number): Promise<boolean> {
  return request.put(`/comments/${id}/audit`, null, { params: { status } })
}
