import { request } from './request'
import type { LoginDTO, RegisterDTO, LoginVO, CaptchaVO, UserInfoVO } from '@/types'

// 邮箱格式验证
function validateEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 登录
export function login(data: LoginDTO): Promise<LoginVO> {
  if (!data) {
    throw new Error('登录参数不能为空')
  }
  // 验证必填字段
  if (!data.username && !data.email) {
    throw new Error('用户名或邮箱不能为空')
  }
  if (!data.password) {
    throw new Error('密码不能为空')
  }
  return request.post('/auth/login', data)
}

// 登出
export function logout(): Promise<void> {
  return request.post('/auth/logout')
}

// 注册
export function register(data: RegisterDTO): Promise<void> {
  if (!data) {
    throw new Error('注册参数不能为空')
  }
  if (!data.username) {
    throw new Error('用户名不能为空')
  }
  if (!data.password) {
    throw new Error('密码不能为空')
  }
  if (data.email && !validateEmail(data.email)) {
    throw new Error('邮箱格式不正确')
  }
  return request.post('/auth/register', data)
}

// 刷新Token
export function refreshToken(token: string): Promise<LoginVO> {
  if (!token || typeof token !== 'string') {
    throw new Error('刷新令牌不能为空')
  }
  return request.post('/auth/refresh', { refreshToken: token })
}

// 获取验证码
export function getCaptcha(): Promise<CaptchaVO> {
  return request.get('/auth/captcha')
}

// 发送邮箱验证码
export function sendEmailCode(email: string): Promise<void> {
  if (!email || typeof email !== 'string') {
    throw new Error('邮箱不能为空')
  }
  if (!validateEmail(email)) {
    throw new Error('邮箱格式不正确')
  }
  return request.post('/auth/email/code', { email })
}

// 重置密码
export function resetPassword(data: { 
  email: string
  code: string
  password: string
  username?: string
}): Promise<void> {
  if (!data) {
    throw new Error('重置密码参数不能为空')
  }
  if (!data.email || !validateEmail(data.email)) {
    throw new Error('请输入有效的邮箱地址')
  }
  if (!data.code) {
    throw new Error('验证码不能为空')
  }
  if (!data.password) {
    throw new Error('新密码不能为空')
  }
  if (data.password.length < 6) {
    throw new Error('密码长度不能少于6位')
  }
  return request.post('/auth/password/reset', data)
}

// 获取当前用户信息
export function getCurrentUser(): Promise<UserInfoVO> {
  return request.get('/auth/info')
}
