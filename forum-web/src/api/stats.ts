import { request } from './request'

export interface OverviewStatsVO {
  userCount: number
  postCount: number
  commentCount: number
  todayActive: number
  todayPostCount: number
  todayCommentCount: number
}

export interface TrendDataVO {
  dates: string[]
  userData: number[]
  postData: number[]
  commentData: number[]
  activeData: number[]
}

export interface UserStatsVO {
  totalUsers: number
  activeUsers: number
  newUsersToday: number
  newUsersThisWeek: number
  newUsersThisMonth: number
}

export interface PostStatsVO {
  totalPosts: number
  publishedPosts: number
  pendingPosts: number
  todayPosts: number
  weekPosts: number
  monthPosts: number
}

export interface InteractionStatsVO {
  totalComments: number
  totalLikes: number
  totalCollections: number
  todayComments: number
  todayLikes: number
  todayCollections: number
}

export function getOverviewStats(): Promise<OverviewStatsVO> {
  return request.get('/stats/overview')
}

export function getUserStats(): Promise<UserStatsVO> {
  return request.get('/stats/user')
}

export function getPostStats(): Promise<PostStatsVO> {
  return request.get('/stats/post')
}

export function getInteractionStats(): Promise<InteractionStatsVO> {
  return request.get('/stats/interaction')
}

export function getTrendData(rangeType: 'day' | 'week' | 'month' = 'day'): Promise<TrendDataVO> {
  // 参数验证
  if (!['day', 'week', 'month'].includes(rangeType)) {
    throw new Error('rangeType必须是day、week或month')
  }
  return request.get('/stats/trend', { rangeType })
}
