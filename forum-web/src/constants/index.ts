/**
 * 用户状态常量
 */
export const USER_STATUS = {
  /** 禁用状态 */
  DISABLED: 0,
  /** 正常状态 */
  NORMAL: 1,
  /** 待审核状态 */
  PENDING: 2
} as const

export type UserStatusType = typeof USER_STATUS[keyof typeof USER_STATUS]

/**
 * 用户状态文本映射
 */
export const USER_STATUS_TEXT: Record<number, string> = {
  [USER_STATUS.DISABLED]: '禁用',
  [USER_STATUS.NORMAL]: '正常',
  [USER_STATUS.PENDING]: '待审核'
}

/**
 * 所有有效的用户状态值
 */
export const VALID_USER_STATUSES: number[] = Object.values(USER_STATUS)

/**
 * 通知状态常量
 */
export const NOTICE_STATUS = {
  /** 草稿 */
  DRAFT: 0,
  /** 已发布 */
  PUBLISHED: 1,
  /** 已撤回 */
  REVOKED: 2
} as const

/**
 * 通知类型常量
 */
export const NOTICE_TYPE = {
  /** 系统通知 */
  SYSTEM: 0,
  /** 公告 */
  ANNOUNCEMENT: 1,
  /** 活动 */
  ACTIVITY: 2
} as const

/**
 * 举报状态常量
 */
export const REPORT_STATUS = {
  /** 待处理 */
  PENDING: 0,
  /** 处理中 */
  PROCESSING: 1,
  /** 已处理 */
  RESOLVED: 2,
  /** 已驳回 */
  REJECTED: 3
} as const

/**
 * 举报类型常量
 */
export const REPORT_TYPE = {
  /** 帖子 */
  POST: 1,
  /** 评论 */
  COMMENT: 2,
  /** 用户 */
  USER: 3
} as const
