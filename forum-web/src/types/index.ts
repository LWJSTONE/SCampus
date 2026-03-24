// 角色类型定义
export interface RoleVO {
  id?: number
  roleCode?: string
  roleName?: string
  description?: string
}

// 角色字段可以是字符串数组或角色对象数组
export type RoleType = string | RoleVO

// 用户相关类型
export interface UserVO {
  id: number
  username: string
  nickname: string
  avatar: string
  bio?: string  // 个人简介
  signature?: string  // 兼容旧字段名
  school?: string
  major?: string
  grade?: string
  gender?: number
  status: number
  createTime: string
}

export interface UserDetailVO extends UserVO {
  email: string
  phone: string
  experience: number
  integral: number
  postCount: number
  commentCount: number
  followerCount: number
  followingCount: number
  roles: RoleType[]
  permissions: string[]
  // 关注状态
  isFollowing?: boolean
  followed?: boolean
}

export interface UserQueryDTO {
  page: number
  size: number
  current?: number
  keyword?: string
  status?: number
  orderBy?: string
}

export interface UserUpdateDTO {
  nickname?: string
  avatar?: string
  bio?: string  // 个人简介（后端字段名）
  signature?: string  // 兼容前端旧字段名
  school?: string
  major?: string
  grade?: string
  gender?: number
  email?: string
  phone?: string
}

// 认证相关类型
export interface LoginDTO {
  username?: string  // 用户名登录时使用（可选，与email二选一）
  email?: string     // 邮箱登录时使用（可选，与username二选一）
  password: string
  captcha?: string
  captchaKey?: string
  rememberMe?: boolean
}

export interface RegisterDTO {
  username: string
  password: string
  confirmPassword?: string
  email: string
  captcha?: string
  captchaKey?: string
  code?: string // 邮箱验证码
  nickname?: string
  phone?: string
  schoolId?: number
  studentNo?: string
}

export interface LoginVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
  tokenType?: string
  userId?: number
  username?: string
  nickname?: string
  avatar?: string
  roles?: string[]
}

export interface CaptchaVO {
  captchaImage: string
  captchaKey: string
  expireTime?: number
  // 兼容旧字段名
  image?: string
  key?: string
}

export interface UserInfoVO {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  roles: RoleType[]
  permissions: string[]
}

// 帖子相关类型
export interface PostVO {
  id: number
  title: string
  summary: string
  content?: string
  username: string  // 前端统一使用username
  userName?: string // 兼容后端返回的字段名
  userAvatar: string
  userId: number
  forumId: number
  forumName: string
  cover?: string
  coverImage?: string
  viewCount: number
  commentCount: number
  likeCount: number
  collectCount: number
  isTop: boolean
  isEssence: boolean
  createTime: string
  updateTime: string
  hotScore?: number
  isAuthor?: boolean
  isLiked?: boolean
  isCollected?: boolean
}

export interface PostDetailVO extends PostVO {
  type: number
  status: number
  isAnonymous: boolean
  ipAddress: string
  tags: TagVO[]
  attachments: AttachmentVO[]
  liked?: boolean
  collected?: boolean
  authorId?: number
  authorName?: string
  authorAvatar?: string
  categoryName?: string
}

export interface TagVO {
  id: number
  name: string
}

export interface PostQueryDTO {
  page: number
  size: number
  current?: number
  forumId?: number
  userId?: number
  keyword?: string
  status?: number
  orderBy?: string
  order?: 'asc' | 'desc'
  sortType?: number
  isTop?: number
  isEssence?: number
  type?: number
}

export interface PostCreateDTO {
  forumId: number
  title: string
  content: string
  type?: number
  isAnonymous?: boolean
  tags?: string[]
  tagIds?: number[]
  attachmentIds?: number[]
  summary?: string
  coverImage?: string
  attachments?: AttachmentDTO[]
}

export interface AttachmentDTO {
  type: string
  name: string
  url: string
  thumbnailUrl?: string
  size?: number
  mimeType?: string
  width?: number
  height?: number
  duration?: number
}

// 评论相关类型
export interface CommentVO {
  id: number
  postId: number
  parentId: number
  rootId?: number
  userId: number
  username: string  // 前端统一使用username
  userName?: string // 兼容后端返回的字段名
  userAvatar: string
  content: string
  likeCount: number
  replyCount: number
  createTime: string
  children?: CommentVO[]
  replies?: CommentVO[] // 兼容后端返回的字段名
  authorName?: string
  replyToUsername?: string
  replyToUserId?: number
  replyToUserName?: string // 兼容后端返回的字段名
  isLiked?: boolean
  isAuthor?: boolean
}

export interface CommentCreateDTO {
  postId: number
  parentId?: number
  rootId?: number
  replyToUserId?: number
  content: string
}

// 版块相关类型
export interface CategoryVO {
  id: number
  name: string
  code: string
  icon: string
  description: string
  sortOrder: number
  status?: number
  children?: CategoryVO[]
  forums?: ForumVO[]
}

export interface ForumVO {
  id: number
  categoryId: number
  name: string
  description: string
  coverUrl?: string
  icon?: string
  postCount: number
  threadCount?: number
  status?: number
  sortOrder?: number
  moderators?: UserVO[]
}

// 通知相关类型
export interface NoticeVO {
  id: number
  title: string
  content: string
  type: number
  status: number
  publisherId?: number
  publisherName?: string
  isRead?: boolean
  readTime?: string
  createTime: string
  updateTime?: string
}

// 附件相关类型
export interface AttachmentVO {
  id: number
  name: string
  url: string
  size: number
  type: string
  mimeType?: string
  thumbnailUrl?: string
  width?: number
  height?: number
}

// 通用类型
export interface PageQuery {
  page: number
  size: number
  // 兼容后端参数
  current?: number
}

// 分页结果类型
export interface PageResult<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
  list?: T[]
}

// API 响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 类型别名（向后兼容）
export type PostDetail = PostDetailVO
export type Comment = CommentVO
export type Category = CategoryVO
export type Forum = ForumVO
