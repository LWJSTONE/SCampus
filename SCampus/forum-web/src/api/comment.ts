/**
 * 评论相关 API
 * 处理评论的增删改查、点赞等接口
 */
import { get, post, put, del } from './request'
import { Comment, CommentCreateParams, PageQuery, PageResult } from '@/types'

/**
 * 评论查询参数
 */
interface CommentQueryParams extends PageQuery {
  postId: number
  orderBy?: string
}

/**
 * 获取评论列表
 * @param params 查询参数
 */
export function getCommentList(params: CommentQueryParams) {
  return get<PageResult<Comment>>('/comment/list', params)
}

/**
 * 获取帖子的评论（树形结构）
 * @param postId 帖子ID
 * @param params 分页参数
 */
export function getPostComments(postId: number, params: PageQuery) {
  return get<PageResult<Comment>>(`/comment/post/${postId}`, params)
}

/**
 * 创建评论
 * @param data 评论数据
 */
export function createComment(data: CommentCreateParams) {
  return post<{ id: number }>('/comment', data)
}

/**
 * 删除评论
 * @param id 评论ID
 */
export function deleteComment(id: number) {
  return del(`/comment/${id}`)
}

/**
 * 点赞评论
 * @param id 评论ID
 */
export function likeComment(id: number) {
  return post(`/comment/${id}/like`)
}

/**
 * 取消点赞评论
 * @param id 评论ID
 */
export function unlikeComment(id: number) {
  return post(`/comment/${id}/unlike`)
}

/**
 * 获取用户的评论
 * @param userId 用户ID
 * @param params 分页参数
 */
export function getUserComments(userId: number, params: PageQuery) {
  return get<PageResult<Comment>>(`/comment/user/${userId}`, params)
}

/**
 * 管理员审核评论
 * @param id 评论ID
 * @param status 状态
 */
export function auditComment(id: number, status: number) {
  return put(`/comment/${id}/audit`, { status })
}
