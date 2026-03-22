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

export function getOverviewStats(): Promise<OverviewStatsVO> {
  return request.get('/stats/overview')
}

export function getUserStats(): Promise<any> {
  return request.get('/stats/user')
}

export function getPostStats(): Promise<any> {
  return request.get('/stats/post')
}

export function getInteractionStats(): Promise<any> {
  return request.get('/stats/interaction')
}

export function getTrendData(rangeType: 'day' | 'week' | 'month' = 'day'): Promise<TrendDataVO> {
  return request.get('/stats/trend', { rangeType })
}
