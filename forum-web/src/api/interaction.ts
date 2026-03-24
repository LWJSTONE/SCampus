/**
 * 互动服务API
 * 提供点赞、收藏、@提及等互动功能的接口
 */
import { request, PageResult } from './request'

// ==================== 类型定义 ====================

export interface LikeDTO {
  targetType: number  // 目标类型：1-帖子 2-评论
  targetId: number    // 目标ID
}

export interface LikeResultVO {
  isLike: boolean
  message: string
  likeCount: number
}

export interface CollectDTO {
  postId: number      // 帖子ID
  folderId?: number   // 收藏夹ID（可选）
}

export interface CollectResultVO {
  isCollect: boolean
  message: string
  collectCount: number
}

export interface CollectVO {
  id: number
  postId: number
  postTitle: string
  postAuthor: string
  collectTime: string
}

export interface MentionDTO {
  targetType: number  // 目标类型：1-帖子 2-评论
  targetId: number    // 目标ID
  mentionedUserId: number  // 被提及的用户ID
  content: string     // 提及内容
}

// ==================== 点赞相关接口 ====================

/**
 * 点赞/取消点赞（toggle模式）
 * @param data 点赞参数
 */
export function like(data: LikeDTO): Promise<LikeResultVO> {
  // 参数验证
  if (!data) {
    throw new Error('点赞参数不能为空')
  }
  if (data.targetType !== 1 && data.targetType !== 2) {
    throw new Error('目标类型无效，必须是1(帖子)或2(评论)')
  }
  if (!data.targetId || isNaN(data.targetId) || data.targetId <= 0) {
    throw new Error('无效的目标ID')
  }
  return request.post('/interactions/like', data)
}

/**
 * 检查点赞状态
 * @param targetType 目标类型：1-帖子 2-评论
 * @param targetId 目标ID
 */
export function checkLike(targetType: number, targetId: number): Promise<{ isLiked: boolean; likeCount: number }> {
  if (targetType !== 1 && targetType !== 2) {
    throw new Error('目标类型无效，必须是1(帖子)或2(评论)')
  }
  if (!targetId || isNaN(targetId) || targetId <= 0) {
    throw new Error('无效的目标ID')
  }
  return request.get('/interactions/like/check', { targetType, targetId })
}

// ==================== 收藏相关接口 ====================

/**
 * 收藏/取消收藏（toggle模式）
 * @param data 收藏参数
 */
export function collect(data: CollectDTO): Promise<CollectResultVO> {
  // 参数验证
  if (!data) {
    throw new Error('收藏参数不能为空')
  }
  if (!data.postId || isNaN(data.postId) || data.postId <= 0) {
    throw new Error('无效的帖子ID')
  }
  return request.post('/interactions/collect', data)
}

/**
 * 检查收藏状态
 * @param postId 帖子ID
 */
export function checkCollect(postId: number): Promise<{ isCollected: boolean; collectCount: number }> {
  if (!postId || isNaN(postId) || postId <= 0) {
    throw new Error('无效的帖子ID')
  }
  return request.get('/interactions/collect/check', { postId })
}

/**
 * 获取收藏列表
 * @param params 分页参数
 */
export function getCollectList(params: { page: number; size: number }): Promise<PageResult<CollectVO>> {
  const queryParams = {
    current: params.page || 1,
    size: Math.max(1, Math.min(100, params.size || 10))
  }
  return request.get('/interactions/collect/list', queryParams)
}

// ==================== @提及相关接口 ====================

/**
 * 创建@提及
 * @param data 提及参数
 */
export function createMention(data: MentionDTO): Promise<number> {
  // 参数验证
  if (!data) {
    throw new Error('提及参数不能为空')
  }
  if (data.targetType !== 1 && data.targetType !== 2) {
    throw new Error('目标类型无效，必须是1(帖子)或2(评论)')
  }
  if (!data.targetId || isNaN(data.targetId) || data.targetId <= 0) {
    throw new Error('无效的目标ID')
  }
  if (!data.mentionedUserId || isNaN(data.mentionedUserId) || data.mentionedUserId <= 0) {
    throw new Error('无效的被提及用户ID')
  }
  return request.post('/interactions/mention', data)
}

/**
 * 获取用户的@提及列表
 * @param params 分页参数
 */
export function getMentionList(params: { page: number; size: number }): Promise<PageResult<any>> {
  const queryParams = {
    current: params.page || 1,
    size: Math.max(1, Math.min(100, params.size || 10))
  }
  return request.get('/interactions/mention/list', queryParams)
}
