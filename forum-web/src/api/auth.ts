import { request } from './request'
import type { LoginDTO, RegisterDTO, LoginVO, CaptchaVO, UserInfoVO } from '@/types'

// 邮箱格式验证
function validateEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 用户名格式验证（防御性编程）
function validateUsername(username: string): { valid: boolean; message: string } {
  const trimmed = username.trim()
  if (!trimmed) {
    return { valid: false, message: '用户名不能为空' }
  }
  if (trimmed.length < 3 || trimmed.length > 20) {
    return { valid: false, message: '用户名长度为 3 到 20 个字符' }
  }
  // 只允许字母、数字、下划线和中文
  const usernamePattern = /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/
  if (!usernamePattern.test(trimmed)) {
    return { valid: false, message: '用户名只能包含字母、数字、下划线和中文' }
  }
  return { valid: true, message: '' }
}

// 密码强度验证
function validatePassword(password: string): { valid: boolean; message: string } {
  if (!password) {
    return { valid: false, message: '密码不能为空' }
  }
  if (password.length < 6 || password.length > 20) {
    return { valid: false, message: '密码长度为 6 到 20 个字符' }
  }
  // 密码必须包含字母和数字
  if (!/^(?=.*[a-zA-Z])(?=.*\d).+$/.test(password)) {
    return { valid: false, message: '密码必须包含字母和数字' }
  }
  return { valid: true, message: '' }
}

// 登录
export function login(data: LoginDTO): Promise<LoginVO> {
  if (!data) {
    throw new Error('登录参数不能为空')
  }
  // 验证必填字段
  const username = data.username?.trim() || ''
  const email = data.email?.trim() || ''
  
  if (!username && !email) {
    throw new Error('用户名或邮箱不能为空')
  }
  if (!data.password) {
    throw new Error('密码不能为空')
  }
  
  // 验证用户名格式（如果使用用户名登录）
  if (username) {
    const validation = validateUsername(username)
    if (!validation.valid) {
      throw new Error(validation.message)
    }
  }
  
  // 验证邮箱格式（如果使用邮箱登录）
  if (email && !validateEmail(email)) {
    throw new Error('邮箱格式不正确')
  }
  
  // 返回清理后的数据
  return request.post('/auth/login', {
    ...data,
    username: username || undefined,
    email: email || undefined
  })
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
  
  // 用户名验证
  const username = data.username?.trim() || ''
  if (!username) {
    throw new Error('用户名不能为空')
  }
  const usernameValidation = validateUsername(username)
  if (!usernameValidation.valid) {
    throw new Error(usernameValidation.message)
  }
  
  // 密码验证
  if (!data.password) {
    throw new Error('密码不能为空')
  }
  const passwordValidation = validatePassword(data.password)
  if (!passwordValidation.valid) {
    throw new Error(passwordValidation.message)
  }
  
  // 验证密码确认
  if (data.confirmPassword !== undefined && data.password !== data.confirmPassword) {
    throw new Error('两次输入的密码不一致')
  }
  
  // 邮箱验证
  const email = data.email?.trim() || ''
  if (!email) {
    throw new Error('邮箱不能为空')
  }
  if (!validateEmail(email)) {
    throw new Error('邮箱格式不正确')
  }
  
  // 验证码验证
  const code = data.code?.trim() || ''
  if (!code) {
    throw new Error('验证码不能为空')
  }
  
  // 返回清理后的数据
  return request.post('/auth/register', {
    ...data,
    username,
    email,
    code
  })
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
  const trimmedEmail = email.trim()
  if (!trimmedEmail) {
    throw new Error('邮箱不能为空')
  }
  if (!validateEmail(trimmedEmail)) {
    throw new Error('邮箱格式不正确')
  }
  return request.post('/auth/email/code', { email: trimmedEmail })
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
  
  // 邮箱验证
  const email = data.email?.trim() || ''
  if (!email || !validateEmail(email)) {
    throw new Error('请输入有效的邮箱地址')
  }
  
  // 验证码验证
  const code = data.code?.trim() || ''
  if (!code) {
    throw new Error('验证码不能为空')
  }
  
  // 密码验证
  if (!data.password) {
    throw new Error('新密码不能为空')
  }
  const passwordValidation = validatePassword(data.password)
  if (!passwordValidation.valid) {
    throw new Error(passwordValidation.message)
  }
  
  // 用户名验证（如果提供）
  const username = data.username?.trim() || ''
  if (username) {
    const usernameValidation = validateUsername(username)
    if (!usernameValidation.valid) {
      throw new Error(usernameValidation.message)
    }
  }
  
  // 返回清理后的数据
  return request.post('/auth/password/reset', {
    email,
    code,
    password: data.password,
    username: username || undefined
  })
}

// 获取当前用户信息
export function getCurrentUser(): Promise<UserInfoVO> {
  return request.get('/auth/info')
}
