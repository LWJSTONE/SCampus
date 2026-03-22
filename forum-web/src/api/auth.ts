import { request } from './request'
import type { LoginDTO, RegisterDTO, LoginVO, CaptchaVO, UserInfoVO } from '@/types'

// 登录
export function login(data: LoginDTO): Promise<LoginVO> {
  return request.post('/auth/login', data)
}

// 登出
export function logout(): Promise<void> {
  return request.post('/auth/logout')
}

// 注册
export function register(data: RegisterDTO): Promise<void> {
  return request.post('/auth/register', data)
}

// 刷新Token
export function refreshToken(refreshToken: string): Promise<LoginVO> {
  return request.post('/auth/refresh', { refreshToken })
}

// 获取验证码
export function getCaptcha(): Promise<CaptchaVO> {
  return request.get('/auth/captcha')
}

// 发送邮箱验证码
export function sendEmailCode(email: string): Promise<void> {
  return request.post('/auth/email/code', { email })
}

// 重置密码
export function resetPassword(data: { 
  email: string
  code: string
  password: string
}): Promise<void> {
  return request.post('/auth/password/reset', data)
}

// 获取当前用户信息
export function getCurrentUser(): Promise<UserInfoVO> {
  return request.get('/auth/info')
}
