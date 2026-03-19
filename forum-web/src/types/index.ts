// 用户相关类型
export interface UserVO {
  id: number
  username: string
  nickname: string
  avatar: string
  signature: string
  school: string
  grade: number
  status: number
  createTime: string
}

export interface UserDetailVO extends UserVO {
  email: string
  phone: string
  gender: number
  experience: number
  integral: number
  postCount: number
  commentCount: number
  followerCount: number
  followingCount: number
  roles: string[]
}

export interface UserQueryDTO {
  page: number
  size: number
  keyword?: string
  status?: number
  orderBy?: string
}

export interface UserUpdateDTO {
  nickname?: string
  avatar?: string
  signature?: string
  school?: string
  gender?: number
}

// 认证相关类型
export interface LoginDTO {
  username: string
  password: string
  captcha?: string
  captchaKey?: string
  rememberMe?: boolean
}

export interface RegisterDTO {
  username: string
  password: string
  confirmPassword: string
  email: string
  captcha: string
  captchaKey: string
  code?: string
}

export interface LoginVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface CaptchaVO {
  image: string
  key: string
}

export interface UserInfoVO {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  roles: string[]
  permissions: string[]
}

// 帖子相关类型
export interface PostVO {
  id: number
  title: string
  summary: string
  content: string
  username: string
  userAvatar: string
  forumId: number
  forumName: string
  cover: string
  viewCount: number
  commentCount: number
  likeCount: number
  collectCount: number
  isTop: boolean
  isEssence: boolean
  createTime: string
  updateTime: string
}

export interface PostDetailVO extends PostVO {
  userId: number
  type: number
  status: number
  isAnonymous: boolean
  ipAddress: string
  tags: string[]
  attachments: AttachmentVO[]
  liked?: boolean
  collected?: boolean
  authorId?: number
  authorName?: string
  authorAvatar?: string
  categoryName?: string
}

export interface PostQueryDTO {
  page: number
  size: number
  forumId?: number
  userId?: number
  keyword?: string
  status?: number
  orderBy?: string
  order?: 'asc' | 'desc'
}

export interface PostCreateDTO {
  forumId: number
  title: string
  content: string
  type: number
  isAnonymous: boolean
  tags: string[]
  attachmentIds: number[]
}

// 评论相关类型
export interface CommentVO {
  id: number
  postId: number
  parentId: number
  userId: number
  username: string
  userAvatar: string
  content: string
  likeCount: number
  replyCount: number
  createTime: string
  children?: CommentVO[]
  authorName?: string
  rootId?: number
}

export interface CommentCreateDTO {
  postId: number
  parentId?: number
  replyToUserId?: number
  content: string
}

// 评论创建参数（兼容旧代码）
export interface CommentCreateParams {
  postId: number
  parentId?: number
  replyToId?: number
  rootId?: number
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
  children?: CategoryVO[]
}

export interface ForumVO {
  id: number
  categoryId: number
  name: string
  description: string
  coverUrl: string
  postCount: number
  threadCount: number
  moderators: UserVO[]
}

// 通知相关类型
export interface NoticeVO {
  id: number
  title: string
  content: string
  type: number
  isRead: boolean
  createTime: string
}

// 附件相关类型
export interface AttachmentVO {
  id: number
  name: string
  url: string
  size: number
  type: string
}

// 通用类型
export interface PageQuery {
  page: number
  size: number
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

// 类型别名（向后兼容）
export type PostDetail = PostDetailVO
export type Comment = CommentVO
export type Category = CategoryVO
export type Forum = ForumVO
